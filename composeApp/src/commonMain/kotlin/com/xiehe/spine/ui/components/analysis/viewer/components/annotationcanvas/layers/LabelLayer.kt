package com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.layers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.data.measurement.MeasurementPoint
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import com.xiehe.spine.ui.components.analysis.viewer.domain.calculateSmartTagPosition
import com.xiehe.spine.ui.components.analysis.viewer.domain.formatMeasurementTag
import com.xiehe.spine.ui.components.analysis.viewer.domain.resolveAnnotationColor
import com.xiehe.spine.ui.components.analysis.viewer.domain.resolveMeasurementTagAnchor
import com.xiehe.spine.ui.components.analysis.viewer.domain.shouldShowMetricTag
import com.xiehe.spine.ui.components.text.shared.Text
import com.xiehe.spine.ui.theme.SpineTheme
import kotlin.math.roundToInt

@Composable
fun LabelLayer(
    measurements: List<AnnotationMeasurement>,
    pendingPoints: List<MeasurementPoint>,
    sx: Float,
    sy: Float,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    val toolColors = colors.annotationTools
    val occupiedLabelPositions = mutableListOf<Offset>()

    Box(modifier = modifier.fillMaxSize()) {
        measurements.forEach { measurement ->
            if (measurement.kind == AnnotationMeasurementKind.DETECTED) {
                val point = measurement.points.firstOrNull() ?: return@forEach
                val pointLabel = measurement.pointLabel ?: return@forEach
                val x = (point.x * sx).toFloat()
                val y = (point.y * sy).toFloat()
                Text(
                    text = pointLabel,
                    style = SpineTheme.typography.caption.copy(fontSize = 8.sp),
                    color = toolColors.detectedPoint,
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (x + 6f).roundToInt(),
                            y = (y - 14f).roundToInt(),
                        )
                    },
                    maxLines = 1,
                )
            }

            if (shouldShowMetricTag(measurement)) {
                val baseAnchor = resolveMeasurementTagAnchor(measurement, sx, sy)
                val tagPosition = calculateSmartTagPosition(baseAnchor, occupiedLabelPositions)
                occupiedLabelPositions += tagPosition
                Text(
                    text = formatMeasurementTag(measurement),
                    style = SpineTheme.typography.caption.copy(fontSize = 7.sp),
                    color = resolveAnnotationColor(measurement, toolColors),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (tagPosition.x + 4f).roundToInt(),
                                y = (tagPosition.y - 12f).roundToInt(),
                            )
                        }
                        .background(
                            color = toolColors.labelBackground,
                            shape = RoundedCornerShape(5.dp),
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp),
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
                color = toolColors.draft,
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
