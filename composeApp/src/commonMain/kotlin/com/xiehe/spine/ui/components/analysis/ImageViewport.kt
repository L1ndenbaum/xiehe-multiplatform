package com.xiehe.spine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import com.xiehe.spine.ui.theme.SpineTheme
import kotlin.math.roundToInt
import com.xiehe.spine.ui.viewmodel.AnalysisMeasurementKind

@Composable
fun ImageViewport(
    bitmap: ImageBitmap?,
    measurements: List<com.xiehe.spine.ui.viewmodel.ImageAnalysisMeasurement>,
    hiddenKeys: Set<String>,
    zoomPercent: Int,
    contrast: Int,
    brightness: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    key(bitmap) {
        var gestureScale by remember { mutableFloatStateOf(1f) }
        var panOffset by remember { mutableStateOf(Offset.Zero) }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .clipToBounds()
                .background(Color(0xFF0B1622))
                .pointerInput(bitmap) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        gestureScale = (gestureScale * zoom).coerceIn(0.7f, 6f)
                        panOffset += pan
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap == null) {
                Text(
                    text = "影像加载中...",
                    style = SpineTheme.typography.body,
                    color = colors.textTertiary,
                )
                return@BoxWithConstraints
            }

            val density = LocalDensity.current
            val imageSize = remember(bitmap, constraints.maxWidth, constraints.maxHeight, density.density) {
                computeFitSize(
                    bitmapWidth = bitmap.width,
                    bitmapHeight = bitmap.height,
                    maxWidth = constraints.maxWidth,
                    density = density,
                )
            }
            val totalScale = (zoomPercent / 100f) * gestureScale
            val colorFilter = remember(contrast, brightness) {
                ColorFilter.colorMatrix(createAdjustMatrix(contrast = contrast, brightness = brightness))
            }
            val computedMeasurements = remember(measurements) {
                measurements.filter { it.kind == AnalysisMeasurementKind.COMPUTED }
            }
            val detectedPoints = remember(measurements) {
                measurements.filter { it.kind == AnalysisMeasurementKind.DETECTED }
            }
            val imageWidthPx = with(density) { imageSize.width.toPx() }
            val imageHeightPx = with(density) { imageSize.height.toPx() }
            val sx = imageWidthPx / bitmap.width
            val sy = imageHeightPx / bitmap.height

            Box(
                modifier = Modifier
                    .size(imageSize)
                    .graphicsLayer {
                        scaleX = totalScale
                        scaleY = totalScale
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
                    computedMeasurements.forEach { item ->
                        if (hiddenKeys.contains(item.key)) return@forEach
                        val points = item.points.map { point ->
                            Offset(
                                x = (point.x * sx).toFloat(),
                                y = (point.y * sy).toFloat(),
                            )
                        }
                        if (points.isEmpty()) return@forEach

                        if (points.size >= 2) {
                            drawLine(
                                color = Color(0xFFE53935),
                                start = points[0],
                                end = points[1],
                                strokeWidth = 3.2f,
                            )
                        }

                        points.forEach { center ->
                            drawCircle(
                                color = Color(0xFFE53935),
                                radius = 5f,
                                center = center,
                            )
                        }
                    }

                    detectedPoints.forEach { item ->
                        if (hiddenKeys.contains(item.key)) return@forEach
                        val point = item.points.firstOrNull() ?: return@forEach
                        drawCircle(
                            color = Color(0xFF37B24D),
                            radius = 4.8f,
                            center = Offset((point.x * sx).toFloat(), (point.y * sy).toFloat()),
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    detectedPoints.forEach { item ->
                        if (hiddenKeys.contains(item.key)) return@forEach
                        val point = item.points.firstOrNull() ?: return@forEach
                        val pointLabel = item.pointLabel ?: return@forEach
                        val x = (point.x * sx).toFloat()
                        val y = (point.y * sy).toFloat()
                        Text(
                            text = pointLabel,
                            style = SpineTheme.typography.caption.copy(fontSize = 8.sp),
                            color = Color(0xFF37B24D),
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (x + 6f).roundToInt(),
                                        y = (y - 17f).roundToInt(),
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
