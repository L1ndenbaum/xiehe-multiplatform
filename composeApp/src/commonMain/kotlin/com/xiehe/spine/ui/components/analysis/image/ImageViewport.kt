package com.xiehe.spine.ui.components.analysis.image

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.data.MeasurementPoint
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.image.AnalysisMeasurementKind
import com.xiehe.spine.ui.viewmodel.image.ImageAnalysisMeasurement
import com.xiehe.spine.ui.viewmodel.image.TOOL_AUX_POLYGON
import com.xiehe.spine.ui.viewmodel.image.TOOL_MOVE
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun ImageViewport(
    bitmap: ImageBitmap?,
    measurements: List<ImageAnalysisMeasurement>,
    hiddenKeys: Set<String>,
    activeToolId: String,
    pendingPoints: List<MeasurementPoint>,
    isImageLocked: Boolean,
    zoomPercent: Int,
    contrast: Int,
    brightness: Int,
    onCanvasTap: (MeasurementPoint) -> Unit,
    onCanvasDoubleTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    key(bitmap) {
        var gestureScale by remember { mutableFloatStateOf(1f) }
        var panOffset by remember { mutableStateOf(Offset.Zero) }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .clipToBounds()
                .background(Color(0xFF0B1622)),
        ) {
            val maxWidthPxInt = constraints.maxWidth
            val maxHeightPxInt = constraints.maxHeight
            val containerWidthPx = maxWidthPxInt.toFloat()
            val containerHeightPx = maxHeightPxInt.toFloat()
            val rawScale = (zoomPercent / 100f) * gestureScale
            val renderedScale by animateFloatAsState(
                targetValue = maxOf(1f, rawScale),
                animationSpec = tween(durationMillis = 220),
                label = "analysis_viewport_rendered_scale",
            )

            LaunchedEffect(bitmap, renderedScale, containerWidthPx, containerHeightPx) {
                val image = bitmap ?: return@LaunchedEffect
                val baseHeight = containerWidthPx / image.width.toFloat() * image.height.toFloat()
                val clamped = clampPanOffset(
                    pan = panOffset,
                    renderedScale = renderedScale,
                    baseImageWidthPx = containerWidthPx,
                    baseImageHeightPx = baseHeight,
                    containerWidthPx = containerWidthPx,
                    containerHeightPx = containerHeightPx,
                )
                if (clamped != panOffset) {
                    panOffset = clamped
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(bitmap, activeToolId, isImageLocked, renderedScale, containerWidthPx, containerHeightPx) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (bitmap == null) return@detectTapGestures
                                if (activeToolId == TOOL_MOVE) return@detectTapGestures
                                val source = screenToSourcePoint(
                                    tapOffset = offset,
                                    bitmap = bitmap,
                                    containerWidthPx = containerWidthPx,
                                    containerHeightPx = containerHeightPx,
                                    panOffset = panOffset,
                                    totalScale = renderedScale,
                                ) ?: return@detectTapGestures
                                onCanvasTap(source)
                            },
                            onDoubleTap = {
                                if (activeToolId == TOOL_AUX_POLYGON) {
                                    onCanvasDoubleTap()
                                }
                            },
                        )
                    }
                    .pointerInput(bitmap, isImageLocked) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (isImageLocked) return@detectTransformGestures
                            val image = bitmap ?: return@detectTransformGestures
                            val nextGestureScale = (gestureScale * zoom).coerceIn(0.4f, 6f)
                            val nextRenderedScale = maxOf(1f, (zoomPercent / 100f) * nextGestureScale)
                            val baseHeight = containerWidthPx / image.width.toFloat() * image.height.toFloat()
                            gestureScale = nextGestureScale
                            panOffset = clampPanOffset(
                                pan = panOffset + pan,
                                renderedScale = nextRenderedScale,
                                baseImageWidthPx = containerWidthPx,
                                baseImageHeightPx = baseHeight,
                                containerWidthPx = containerWidthPx,
                                containerHeightPx = containerHeightPx,
                            )
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap == null) {
                    Text(
                        text = "影像加载中...",
                        style = SpineTheme.typography.body,
                        color = SpineTheme.colors.textTertiary,
                    )
                } else {
                    val density = LocalDensity.current
                    val imageSize = remember(bitmap, maxWidthPxInt, maxHeightPxInt, density.density) {
                        computeFitSize(
                            bitmapWidth = bitmap.width,
                            bitmapHeight = bitmap.height,
                            maxWidth = maxWidthPxInt,
                            density = density,
                        )
                    }
                    val colorFilter = remember(contrast, brightness) {
                        ColorFilter.colorMatrix(createAdjustMatrix(contrast = contrast, brightness = brightness))
                    }
                    val imageWidthPx = with(density) { imageSize.width.toPx() }
                    val imageHeightPx = with(density) { imageSize.height.toPx() }
                    val sx = imageWidthPx / bitmap.width
                    val sy = imageHeightPx / bitmap.height

                    Box(
                        modifier = Modifier
                            .size(imageSize)
                            .graphicsLayer {
                                scaleX = renderedScale
                                scaleY = renderedScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "analysis_image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            colorFilter = colorFilter,
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val visibleMeasurements = measurements.filterNot { hiddenKeys.contains(it.key) }
                            visibleMeasurements.forEach { item ->
                                drawMeasurement(item = item, sx = sx, sy = sy)
                            }

                            val previewPoints = pendingPoints.map { point ->
                                Offset(
                                    x = (point.x * sx).toFloat(),
                                    y = (point.y * sy).toFloat(),
                                )
                            }
                            previewPoints.forEachIndexed { index, point ->
                                drawCircle(
                                    color = AnalysisMeasurementPalette.draftColor,
                                    radius = 5.4f,
                                    center = point,
                                )
                                if (index > 0) {
                                    drawLine(
                                        color = AnalysisMeasurementPalette.draftColor.copy(alpha = 0.86f),
                                        start = previewPoints[index - 1],
                                        end = point,
                                        strokeWidth = 2.2f,
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            val visibleMeasurements = measurements.filterNot { hiddenKeys.contains(it.key) }

                            visibleMeasurements.forEachIndexed { index, item ->
                                if (item.kind == AnalysisMeasurementKind.DETECTED) {
                                    val point = item.points.firstOrNull() ?: return@forEachIndexed
                                    val pointLabel = item.pointLabel ?: return@forEachIndexed
                                    val x = (point.x * sx).toFloat()
                                    val y = (point.y * sy).toFloat()
                                    Text(
                                        text = pointLabel,
                                        style = SpineTheme.typography.caption.copy(fontSize = 8.sp),
                                        color = AnalysisMeasurementPalette.detectedPointColor,
                                        modifier = Modifier.offset {
                                            IntOffset(
                                                x = (x + 6f).roundToInt(),
                                                y = (y - 14f).roundToInt(),
                                            )
                                        },
                                        maxLines = 1,
                                    )
                                }

                                if (AnalysisMeasurementPalette.shouldShowMetricTag(item)) {
                                    val anchor = item.tagAnchor(sx, sy)
                                    val xOffset = (index % 3) * 8
                                    val yOffset = (index % 5) * 9
                                    Text(
                                        text = "${item.type}:${item.value}",
                                        style = SpineTheme.typography.caption.copy(fontSize = 7.sp),
                                        color = AnalysisMeasurementPalette.colorFor(item),
                                        modifier = Modifier
                                            .offset {
                                                IntOffset(
                                                    x = (anchor.x + 4f + xOffset).roundToInt(),
                                                    y = (anchor.y - 12f - yOffset).roundToInt(),
                                                )
                                            }
                                            .background(
                                                AnalysisMeasurementPalette.labelBackground,
                                                androidx.compose.foundation.shape.RoundedCornerShape(5.dp),
                                            )
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        maxLines = 1,
                                    )
                                }
                            }

                            pendingPoints.forEachIndexed { index, point ->
                                val x = (point.x * sx).toFloat()
                                val y = (point.y * sy).toFloat()
                                Text(
                                    text = "${index + 1}",
                                    style = SpineTheme.typography.caption.copy(fontSize = 9.sp),
                                    color = AnalysisMeasurementPalette.draftColor,
                                    modifier = Modifier.offset {
                                        IntOffset(
                                            x = (x + 8f).roundToInt(),
                                            y = (y - 16f).roundToInt(),
                                        )
                                    },
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMeasurement(
    item: ImageAnalysisMeasurement,
    sx: Float,
    sy: Float,
) {
    val color = AnalysisMeasurementPalette.colorFor(item)
    val points = item.points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }

    item.helperSegments.forEach { segment ->
        drawLine(
            color = if (segment.dashed) AnalysisMeasurementPalette.helperDashedColor else color,
            start = Offset((segment.start.x * sx).toFloat(), (segment.start.y * sy).toFloat()),
            end = Offset((segment.end.x * sx).toFloat(), (segment.end.y * sy).toFloat()),
            strokeWidth = if (segment.dashed) 1.8f else 2.6f,
            pathEffect = if (segment.dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null,
        )
    }

    when (item.type) {
        "Cobb" -> {
            if (points.size >= 4) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                drawLine(color = color, start = points[2], end = points[3], strokeWidth = 3f)
            }
        }

        "角度标注" -> {
            if (points.size >= 3) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                drawLine(color = color, start = points[1], end = points[2], strokeWidth = 3f)
            }
        }

        "Circle" -> {
            if (points.size >= 2) {
                val center = Offset((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f)
                val radius = hypot(points[1].x - points[0].x, points[1].y - points[0].y) / 2f
                drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 2.6f))
            }
        }

        "Ellipse" -> {
            if (points.size >= 2) {
                val left = minOf(points[0].x, points[1].x)
                val top = minOf(points[0].y, points[1].y)
                val width = kotlin.math.abs(points[1].x - points[0].x)
                val height = kotlin.math.abs(points[1].y - points[0].y)
                drawOval(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.6f),
                )
            }
        }

        "Box" -> {
            if (points.size >= 2) {
                val left = minOf(points[0].x, points[1].x)
                val top = minOf(points[0].y, points[1].y)
                val width = kotlin.math.abs(points[1].x - points[0].x)
                val height = kotlin.math.abs(points[1].y - points[0].y)
                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.6f),
                )
            }
        }

        "Arrow" -> {
            if (points.size >= 2) {
                drawArrow(points[0], points[1], color)
            }
        }

        "Polygon" -> {
            if (points.size >= 2) {
                val path = Path()
                path.moveTo(points[0].x, points[0].y)
                for (index in 1 until points.size) {
                    path.lineTo(points[index].x, points[index].y)
                }
                path.close()
                drawPath(path = path, color = color, style = Stroke(width = 2.6f))
            }
        }

        else -> {
            if (points.size >= 2) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
            }
        }
    }

    points.forEach { point ->
        drawCircle(color = color, radius = if (item.kind == AnalysisMeasurementKind.DETECTED) 4.5f else 5f, center = point)
    }
}

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
) {
    drawLine(color = color, start = start, end = end, strokeWidth = 2.8f)
    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowLength = 12f
    val wing = 0.48f
    val p1 = Offset(
        x = end.x - arrowLength * kotlin.math.cos(angle - wing),
        y = end.y - arrowLength * kotlin.math.sin(angle - wing),
    )
    val p2 = Offset(
        x = end.x - arrowLength * kotlin.math.cos(angle + wing),
        y = end.y - arrowLength * kotlin.math.sin(angle + wing),
    )
    drawLine(color = color, start = end, end = p1, strokeWidth = 2.8f)
    drawLine(color = color, start = end, end = p2, strokeWidth = 2.8f)
}

private fun ImageAnalysisMeasurement.tagAnchor(
    sx: Float,
    sy: Float,
): Offset {
    if (points.isEmpty()) return Offset.Zero
    if (points.size == 1) {
        return Offset((points[0].x * sx).toFloat(), (points[0].y * sy).toFloat())
    }
    val x = ((points[0].x + points[1].x) / 2.0 * sx).toFloat()
    val y = ((points[0].y + points[1].y) / 2.0 * sy).toFloat()
    return Offset(x, y)
}

private fun screenToSourcePoint(
    tapOffset: Offset,
    bitmap: ImageBitmap,
    containerWidthPx: Float,
    containerHeightPx: Float,
    panOffset: Offset,
    totalScale: Float,
): MeasurementPoint? {
    val imageWidthPx = containerWidthPx
    val fitScale = imageWidthPx / bitmap.width.toFloat()
    val imageHeightPx = bitmap.height * fitScale
    val centerX = containerWidthPx / 2f
    val centerY = containerHeightPx / 2f
    val topLeftX = centerX - imageWidthPx / 2f
    val topLeftY = centerY - imageHeightPx / 2f

    val localX = tapOffset.x - topLeftX - panOffset.x
    val localY = tapOffset.y - topLeftY - panOffset.y
    val imageCenterX = imageWidthPx / 2f
    val imageCenterY = imageHeightPx / 2f
    val unscaledX = (localX - imageCenterX) / totalScale + imageCenterX
    val unscaledY = (localY - imageCenterY) / totalScale + imageCenterY
    if (unscaledX < 0f || unscaledY < 0f || unscaledX > imageWidthPx || unscaledY > imageHeightPx) {
        return null
    }

    return MeasurementPoint(
        x = (unscaledX / imageWidthPx * bitmap.width.toFloat()).toDouble(),
        y = (unscaledY / imageHeightPx * bitmap.height.toFloat()).toDouble(),
    )
}

private fun computeFitSize(
    bitmapWidth: Int,
    bitmapHeight: Int,
    maxWidth: Int,
    density: Density,
): DpSize {
    val scale = maxWidth / bitmapWidth.toFloat()
    return with(density) {
        DpSize(
            width = (bitmapWidth * scale).toDp(),
            height = (bitmapHeight * scale).toDp(),
        )
    }
}

private fun createAdjustMatrix(
    contrast: Int,
    brightness: Int,
): ColorMatrix {
    val c = 1f + contrast / 120f
    val b = brightness * 2f
    return ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, b,
            0f, c, 0f, 0f, b,
            0f, 0f, c, 0f, b,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
}

private fun clampPanOffset(
    pan: Offset,
    renderedScale: Float,
    baseImageWidthPx: Float,
    baseImageHeightPx: Float,
    containerWidthPx: Float,
    containerHeightPx: Float,
): Offset {
    val scaledWidth = baseImageWidthPx * renderedScale
    val scaledHeight = baseImageHeightPx * renderedScale
    val maxX = ((scaledWidth - containerWidthPx) / 2f).coerceAtLeast(0f)
    val maxY = ((scaledHeight - containerHeightPx) / 2f).coerceAtLeast(0f)
    return Offset(
        x = pan.x.coerceIn(-maxX, maxX),
        y = pan.y.coerceIn(-maxY, maxY),
    )
}






