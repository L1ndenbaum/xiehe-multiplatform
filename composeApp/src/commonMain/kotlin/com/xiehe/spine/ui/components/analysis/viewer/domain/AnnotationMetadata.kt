package com.xiehe.spine.ui.components.analysis.viewer.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import com.xiehe.spine.ui.components.analysis.viewer.catalog.AnnotationTagAnchorStyle
import com.xiehe.spine.ui.components.analysis.viewer.catalog.AnnotationToolColorKey
import com.xiehe.spine.ui.components.analysis.viewer.catalog.getAnnotationToolByMeasurementType
import com.xiehe.spine.ui.theme.SpineAnnotationToolColors
import kotlin.math.hypot

internal enum class AnnotationRenderType {
    LINE_WITH_HORIZONTAL_ARC,
    SINGLE_LINE_WITH_HORIZONTAL,
    TWO_DASHED_LINES,
    SACRAL_WITH_PERPENDICULAR,
    SS,
    PI,
    PT,
    VERTICAL_GUIDE_LINES,
    TTS,
    HORIZONTAL_GUIDE_LINES,
    C7_OFFSET,
    TPA,
    SVA,
    THREE_POINT_ANGLE,
    SIMPLE_LINE,
    SINGLE_HORIZONTAL_LINE,
    SINGLE_VERTICAL_LINE,
    CIRCLE,
    ELLIPSE,
    BOX,
    ARROW,
    POLYGON,
    VERTEBRA_CENTER,
}

internal fun resolveAnnotationRenderType(type: String): AnnotationRenderType = when (type) {
    "T1 Tilt",
    "T1 Slope",
    -> AnnotationRenderType.LINE_WITH_HORIZONTAL_ARC

    "CA",
    "Pelvic",
    -> AnnotationRenderType.SINGLE_LINE_WITH_HORIZONTAL

    "Cobb",
    "C2-C7 CL",
    "TK T2-T5",
    "TK T5-T12",
    "T10-L2",
    "LL L1-S1",
    "LL L1-L4",
    "LL L4-S1",
    "角度标注",
    -> AnnotationRenderType.TWO_DASHED_LINES

    "Sacral" -> AnnotationRenderType.SACRAL_WITH_PERPENDICULAR
    "SS" -> AnnotationRenderType.SS
    "PI" -> AnnotationRenderType.PI
    "PT" -> AnnotationRenderType.PT
    "AVT" -> AnnotationRenderType.VERTICAL_GUIDE_LINES
    "TTS" -> AnnotationRenderType.TTS
    "LLD" -> AnnotationRenderType.HORIZONTAL_GUIDE_LINES
    "TS(Trunk Shift)" -> AnnotationRenderType.C7_OFFSET
    "TPA" -> AnnotationRenderType.TPA
    "SVA" -> AnnotationRenderType.SVA
    "角度测量" -> AnnotationRenderType.THREE_POINT_ANGLE
    "辅助水平线" -> AnnotationRenderType.SINGLE_HORIZONTAL_LINE
    "辅助垂直线" -> AnnotationRenderType.SINGLE_VERTICAL_LINE
    "Auxiliary Circle" -> AnnotationRenderType.CIRCLE
    "Auxiliary Ellipse" -> AnnotationRenderType.ELLIPSE
    "Auxiliary Box" -> AnnotationRenderType.BOX
    "Arrow" -> AnnotationRenderType.ARROW
    "Polygons" -> AnnotationRenderType.POLYGON
    "椎体中心" -> AnnotationRenderType.VERTEBRA_CENTER
    else -> AnnotationRenderType.SIMPLE_LINE
}

fun resolveAnnotationColor(
    measurement: AnnotationMeasurement,
    colors: SpineAnnotationToolColors,
): Color {
    if (measurement.kind == AnnotationMeasurementKind.DETECTED) {
        return colors.detectedPoint
    }

    val tool = getAnnotationToolByMeasurementType(measurement.type)
    return tool?.let { colors.resolveColor(it.colorKey) } ?: colors.length
}

fun valueColorFor(
    measurement: AnnotationMeasurement,
    colors: SpineAnnotationToolColors,
): Color = resolveAnnotationColor(measurement, colors)

fun shouldShowMetricTag(measurement: AnnotationMeasurement): Boolean {
    if (measurement.kind != AnnotationMeasurementKind.COMPUTED) return false
    if (measurement.auxiliary) return false
    if (measurement.type == "标准距离") return false
    if (measurement.value == "--") return false
    return measurement.points.size >= 2 || measurement.type == "椎体中心"
}

fun formatMeasurementTag(measurement: AnnotationMeasurement): String =
    "${measurement.type}: ${formatDisplayValue(measurement.value)}"

fun resolveMeasurementTagAnchor(
    measurement: AnnotationMeasurement,
    sx: Float,
    sy: Float,
): Offset {
    val points = measurement.points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }
    if (points.isEmpty()) return Offset.Zero

    val tool = getAnnotationToolByMeasurementType(measurement.type)
    return when (tool?.tagAnchorStyle) {
        AnnotationTagAnchorStyle.MIDPOINT_ABOVE -> midpoint(points.first(), points.last()).copy(
            y = minOf(points.first().y, points.last().y) - 20f,
        )

        AnnotationTagAnchorStyle.AVERAGE_ABOVE -> average(points).copy(y = points.minOf { it.y } - 18f)
        AnnotationTagAnchorStyle.AVERAGE_ABOVE_COMPACT -> average(points).copy(y = points.minOf { it.y } - 24f)
        AnnotationTagAnchorStyle.CENTER -> average(points)
        null -> average(points)
    }
}

fun calculateSmartTagPosition(
    basePosition: Offset,
    occupiedPositions: List<Offset>,
): Offset {
    if (occupiedPositions.isEmpty()) return basePosition

    val verticalOffset = 18f
    val horizontalOffset = 28f
    val overlapThreshold = 54f

    fun overlaps(candidate: Offset): Boolean = occupiedPositions.any { occupied ->
        hypot(candidate.x - occupied.x, candidate.y - occupied.y) < overlapThreshold
    }

    if (!overlaps(basePosition)) return basePosition

    val candidates = listOf(
        basePosition + Offset(horizontalOffset, 0f),
        basePosition + Offset(horizontalOffset, -verticalOffset),
        basePosition + Offset(horizontalOffset, verticalOffset),
        basePosition + Offset(0f, -verticalOffset),
        basePosition + Offset(0f, verticalOffset),
        basePosition + Offset(-horizontalOffset, 0f),
        basePosition + Offset(-horizontalOffset, -verticalOffset),
        basePosition + Offset(-horizontalOffset, verticalOffset),
        basePosition + Offset(0f, -verticalOffset * 2),
        basePosition + Offset(0f, verticalOffset * 2),
    )

    return candidates.firstOrNull { candidate -> !overlaps(candidate) }
        ?: (basePosition + Offset(0f, -verticalOffset * 2.5f))
}

private fun SpineAnnotationToolColors.resolveColor(colorKey: AnnotationToolColorKey): Color = when (colorKey) {
    AnnotationToolColorKey.NONE -> length
    AnnotationToolColorKey.T1_TILT -> t1Tilt
    AnnotationToolColorKey.COBB -> cobb
    AnnotationToolColorKey.CA -> ca
    AnnotationToolColorKey.PELVIC -> pelvic
    AnnotationToolColorKey.SACRAL -> sacral
    AnnotationToolColorKey.AVT -> avt
    AnnotationToolColorKey.TS -> ts
    AnnotationToolColorKey.LLD -> lld
    AnnotationToolColorKey.C7_OFFSET -> c7Offset
    AnnotationToolColorKey.T1_SLOPE -> t1Slope
    AnnotationToolColorKey.CL -> cl
    AnnotationToolColorKey.TK_T2_T5 -> tkT2T5
    AnnotationToolColorKey.TK_T5_T12 -> tkT5T12
    AnnotationToolColorKey.T10_L2 -> t10L2
    AnnotationToolColorKey.LL_L1_S1 -> llL1S1
    AnnotationToolColorKey.LL_L1_L4 -> llL1L4
    AnnotationToolColorKey.LL_L4_S1 -> llL4S1
    AnnotationToolColorKey.TPA -> tpa
    AnnotationToolColorKey.SVA -> sva
    AnnotationToolColorKey.PI -> pi
    AnnotationToolColorKey.PT -> pt
    AnnotationToolColorKey.SS -> ss
    AnnotationToolColorKey.LENGTH -> length
    AnnotationToolColorKey.ANGLE -> angle
    AnnotationToolColorKey.AUXILIARY_CIRCLE -> auxiliaryCircle
    AnnotationToolColorKey.AUXILIARY_ELLIPSE -> auxiliaryEllipse
    AnnotationToolColorKey.AUXILIARY_BOX -> auxiliaryBox
    AnnotationToolColorKey.AUXILIARY_ARROW -> auxiliaryArrow
    AnnotationToolColorKey.AUXILIARY_POLYGON -> auxiliaryPolygon
    AnnotationToolColorKey.VERTEBRA_CENTER -> vertebraCenter
    AnnotationToolColorKey.AUXILIARY_LENGTH -> auxiliaryLength
    AnnotationToolColorKey.AUXILIARY_ANGLE -> auxiliaryAngle
    AnnotationToolColorKey.AUXILIARY_HORIZONTAL_LINE -> auxiliaryHorizontalLine
    AnnotationToolColorKey.AUXILIARY_VERTICAL_LINE -> auxiliaryVerticalLine
}

private fun formatDisplayValue(value: String): String {
    val match = Regex("^(-?\\d+\\.?\\d*)\\s*(.*)$").matchEntire(value) ?: return value
    val numericValue = match.groupValues[1].toDoubleOrNull() ?: return value
    val unit = match.groupValues[2]
    val displayValue = kotlin.math.round(kotlin.math.abs(numericValue)).toInt()
    return "$displayValue$unit"
}

private fun midpoint(first: Offset, second: Offset): Offset = Offset(
    x = (first.x + second.x) / 2f,
    y = (first.y + second.y) / 2f,
)

private fun average(points: List<Offset>): Offset {
    if (points.isEmpty()) return Offset.Zero
    val totalX = points.sumOf { it.x.toDouble() }.toFloat()
    val totalY = points.sumOf { it.y.toDouble() }.toFloat()
    return Offset(x = totalX / points.size, y = totalY / points.size)
}
