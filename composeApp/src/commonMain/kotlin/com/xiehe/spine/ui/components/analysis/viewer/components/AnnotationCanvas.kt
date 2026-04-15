package com.xiehe.spine.ui.components.analysis.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.xiehe.spine.data.measurement.MeasurementPoint
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.canvas.tools.isMoveTool
import com.xiehe.spine.ui.components.analysis.viewer.canvas.tools.supportsDoubleTapFinish
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.layers.ImageLayer
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.layers.LabelLayer
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.layers.MeasurementLayer
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.layers.PreviewLayer
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.state.rememberAnnotationCanvasViewportState
import com.xiehe.spine.ui.components.text.shared.Text
import com.xiehe.spine.ui.theme.SpineTheme

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
    val colors = SpineTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(colors.backgroundElevated),
    ) {
        val maxWidthPx = constraints.maxWidth
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()
        val visibleMeasurements = remember(measurements, hiddenKeys) {
            measurements.filterNot { hiddenKeys.contains(it.key) }
        }
        val viewportState = rememberAnnotationCanvasViewportState(
            bitmap = bitmap,
            zoomPercent = zoomPercent,
            contrast = contrast,
            brightness = brightness,
            isImageLocked = isImageLocked,
            containerWidthPx = containerWidthPx,
            containerHeightPx = containerHeightPx,
            maxWidthPx = maxWidthPx,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, activeToolId, viewportState.renderedScale, viewportState.panOffset) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (bitmap == null || isMoveTool(activeToolId)) return@detectTapGestures
                            val point = viewportState.mapScreenToImagePoint(offset) ?: return@detectTapGestures
                            onCanvasTap(point)
                        },
                        onDoubleTap = {
                            if (supportsDoubleTapFinish(activeToolId)) {
                                onCanvasDoubleTap()
                            }
                        },
                    )
                }
                .pointerInput(bitmap, isImageLocked, viewportState.renderedScale, viewportState.panOffset) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        viewportState.onTransformGesture(pan, zoom)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap == null || viewportState.imageSize == null) {
                Text(
                    text = "影像加载中...",
                    style = SpineTheme.typography.body,
                    color = colors.textTertiary,
                )
                return@Box
            }

            val sx = viewportState.imageWidthPx / bitmap.width
            val sy = viewportState.imageHeightPx / bitmap.height
            Box(
                modifier = Modifier
                    .size(viewportState.imageSize)
                    .graphicsLayer {
                        scaleX = viewportState.renderedScale
                        scaleY = viewportState.renderedScale
                        translationX = viewportState.panOffset.x
                        translationY = viewportState.panOffset.y
                    },
            ) {
                ImageLayer(
                    bitmap = bitmap,
                    colorFilter = viewportState.colorFilter,
                )
                MeasurementLayer(
                    measurements = visibleMeasurements,
                    sx = sx,
                    sy = sy,
                    toolColors = colors.annotationTools,
                )
                PreviewLayer(
                    pendingPoints = pendingPoints,
                    sx = sx,
                    sy = sy,
                    draftColor = colors.annotationTools.draft,
                )
                LabelLayer(
                    measurements = visibleMeasurements,
                    pendingPoints = pendingPoints,
                    sx = sx,
                    sy = sy,
                )
            }
        }
    }
}
