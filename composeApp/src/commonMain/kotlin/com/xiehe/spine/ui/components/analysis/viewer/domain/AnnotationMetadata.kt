package com.xiehe.spine.ui.components.analysis.viewer.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
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

    return colors.colorForType(measurement.type)
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

fun formatMeasurementTag(measurement: AnnotationMeasurement): String = "${measurement.type}:${measurement.value}"

fun resolveMeasurementTagAnchor(
    measurement: AnnotationMeasurement,
    sx: Float,
    sy: Float,
): Offset {
    val points = measurement.points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }
    if (points.isEmpty()) return Offset.Zero

    return when (resolveAnnotationRenderType(measurement.type)) {
        AnnotationRenderType.LINE_WITH_HORIZONTAL_ARC,
        AnnotationRenderType.SINGLE_LINE_WITH_HORIZONTAL,
        AnnotationRenderType.SACRAL_WITH_PERPENDICULAR,
        AnnotationRenderType.SS,
        AnnotationRenderType.SIMPLE_LINE,
        AnnotationRenderType.SINGLE_HORIZONTAL_LINE,
        AnnotationRenderType.SINGLE_VERTICAL_LINE,
        -> midpoint(points.first(), points.last()).copy(
            y = minOf(points.first().y, points.last().y) - 20f,
        )

        AnnotationRenderType.TWO_DASHED_LINES -> average(points).copy(y = points.minOf { it.y } - 24f)

        AnnotationRenderType.VERTICAL_GUIDE_LINES,
        AnnotationRenderType.TTS,
        AnnotationRenderType.C7_OFFSET,
        AnnotationRenderType.SVA,
        -> average(points).copy(y = points.minOf { it.y } - 18f)

        AnnotationRenderType.PI,
        AnnotationRenderType.PT,
        AnnotationRenderType.TPA,
        AnnotationRenderType.THREE_POINT_ANGLE,
        AnnotationRenderType.CIRCLE,
        AnnotationRenderType.ELLIPSE,
        AnnotationRenderType.BOX,
        AnnotationRenderType.ARROW,
        AnnotationRenderType.POLYGON,
        AnnotationRenderType.VERTEBRA_CENTER,
        -> average(points)

        AnnotationRenderType.HORIZONTAL_GUIDE_LINES -> average(points).copy(y = points.minOf { it.y } - 18f)
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

private fun SpineAnnotationToolColors.colorForType(type: String): Color = when (type) {
    "T1 Tilt" -> t1Tilt
    "Cobb" -> cobb
    "CA" -> ca
    "Pelvic" -> pelvic
    "Sacral" -> sacral
    "AVT" -> avt
    "TTS" -> ts
    "LLD" -> lld
    "TS(Trunk Shift)" -> c7Offset
    "T1 Slope" -> t1Slope
    "C2-C7 CL" -> cl
    "TK T2-T5" -> tkT2T5
    "TK T5-T12" -> tkT5T12
    "T10-L2" -> t10L2
    "LL L1-S1" -> llL1S1
    "LL L1-L4" -> llL1L4
    "LL L4-S1" -> llL4S1
    "TPA" -> tpa
    "SVA" -> sva
    "PI" -> pi
    "PT" -> pt
    "SS" -> ss
    "距离标注",
    "长度测量",
    -> auxiliaryLength

    "角度标注",
    "角度测量",
    -> auxiliaryAngle

    "辅助水平线" -> auxiliaryHorizontalLine
    "辅助垂直线" -> auxiliaryVerticalLine
    "Auxiliary Circle" -> auxiliaryCircle
    "Auxiliary Ellipse" -> auxiliaryEllipse
    "Auxiliary Box" -> auxiliaryBox
    "Arrow" -> auxiliaryArrow
    "Polygons" -> auxiliaryPolygon
    "椎体中心" -> vertebraCenter
    else -> length
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
