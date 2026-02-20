package com.xiehe.spine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import com.xiehe.spine.ui.theme.SpineTheme
import kotlin.math.min

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
                .clip(RoundedCornerShape(14.dp))
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
                    maxHeight = constraints.maxHeight,
                    density = density,
                )
            }
            val totalScale = (zoomPercent / 100f) * gestureScale
            val colorFilter = remember(contrast, brightness) {
                ColorFilter.colorMatrix(createAdjustMatrix(contrast = contrast, brightness = brightness))
            }

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
                    val palette = listOf(
                        Color(0xFF4FC3F7),
                        Color(0xFFAB47BC),
                        Color(0xFFFFC107),
                        Color(0xFF66BB6A),
                        Color(0xFFEF5350),
                        Color(0xFF42A5F5),
                    )
                    val sx = size.width / bitmap.width
                    val sy = size.height / bitmap.height
                    measurements.forEachIndexed { index, item ->
                        if (hiddenKeys.contains(item.key)) return@forEachIndexed
                        val color = palette[index % palette.size]
                        val points = item.points.map { point ->
                            Offset(
                                x = (point.x * sx).toFloat(),
                                y = (point.y * sy).toFloat(),
                            )
                        }
                        if (points.isEmpty()) return@forEachIndexed

                        when {
                            points.size == 2 -> {
                                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                            }

                            points.size >= 4 -> {
                                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                                drawLine(color = color, start = points[2], end = points[3], strokeWidth = 3f)
                            }

                            else -> {
                                points.zipWithNext().forEach { (start, end) ->
                                    drawLine(color = color, start = start, end = end, strokeWidth = 3f)
                                }
                            }
                        }

                        points.forEach { center ->
                            drawCircle(
                                color = color,
                                radius = 5f,
                                center = center,
                            )
                        }
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
    maxHeight: Int,
    density: Density,
): DpSize {
    val scale = min(maxWidth / bitmapWidth.toFloat(), maxHeight / bitmapHeight.toFloat())
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
