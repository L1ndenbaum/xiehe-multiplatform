package com.xiehe.spine.ui.components.analysis.viewer.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.data.measurement.MeasurementPoint
import com.xiehe.spine.ui.components.analysis.image.AnalysisMeasurementPalette
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import com.xiehe.spine.ui.components.analysis.viewer.canvas.tools.isMoveTool
import com.xiehe.spine.ui.components.analysis.viewer.canvas.tools.supportsDoubleTapFinish
import com.xiehe.spine.ui.components.analysis.viewer.canvas.transform.clampPanOffset
import com.xiehe.spine.ui.components.analysis.viewer.canvas.transform.computeFitSize
import com.xiehe.spine.ui.components.analysis.viewer.canvas.transform.screenToImagePoint
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers.drawAnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers.tagAnchor
import com.xiehe.spine.ui.components.text.shared.Text
import com.xiehe.spine.ui.theme.SpineTheme
import kotlin.math.roundToInt

@Composable
fun AnnotationCanvas(
    bitmap: ImageBitmap?,
    measurements: List<AnnotationMeasurement>,
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
                .background(SpineTheme.colors.backgroundElevated),
        ) {
            val maxWidthPxInt = constraints.maxWidth
            val maxHeightPxInt = constraints.maxHeight
            val containerWidthPx = maxWidthPxInt.toFloat()
            val containerHeightPx = maxHeightPxInt.toFloat()
            val rawScale = (zoomPercent / 100f) * gestureScale
            val renderedScale by animateFloatAsState(
                targetValue = maxOf(1f, rawScale),
                animationSpec = tween(durationMillis = 220),
                label = "annotation_canvas_rendered_scale",
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
                                if (bitmap == null || isMoveTool(activeToolId)) return@detectTapGestures
                                val source = screenToImagePoint(
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
                                if (supportsDoubleTapFinish(activeToolId)) {
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
                                drawAnnotationMeasurement(item = item, sx = sx, sy = sy)
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
                                if (item.kind == AnnotationMeasurementKind.DETECTED) {
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
