package com.xiehe.spine.ui.components.analysis.viewer.domain

import com.xiehe.spine.data.measurement.MeasurementPoint
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationHelperSegment
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_ANGLE
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AUX_ARROW
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AUX_BOX
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AUX_CIRCLE
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AUX_ELLIPSE
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AUX_POLYGON
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_AVT
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_CA
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_COBB
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_DISTANCE
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_PELVIC
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_SACRAL
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_STANDARD_DISTANCE
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_T1_TILT
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_TS
import com.xiehe.spine.ui.components.analysis.viewer.catalog.TOOL_VERTEBRA_CENTER
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot

const val DEFAULT_STANDARD_DISTANCE_MM = 100.0

data class AnnotationCalibrationContext(
    val standardDistanceMm: Double?,
    val standardDistancePoints: List<MeasurementPoint>,
)

fun createManualAnnotationMeasurement(
    toolId: String,
    points: List<MeasurementPoint>,
    measurementKey: String,
    calibration: AnnotationCalibrationContext,
    standardDistanceMm: Double?,
): AnnotationMeasurement? {
    return when (toolId) {
        TOOL_T1_TILT -> {
            val angle = lineAngleDegrees(points[0], points[1])
            AnnotationMeasurement(
                key = measurementKey,
                type = "T1 Tilt",
                value = formatAngle(angle, signed = true),
                points = points,
                description = "T1椎体倾斜角测量",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_COBB -> {
            val first = lineAngleDegrees(points[0], points[1])
            val second = lineAngleDegrees(points[2], points[3])
            val cobb = acuteAngle(first, second)
            AnnotationMeasurement(
                key = measurementKey,
                type = "Cobb",
                value = formatAngle(cobb, signed = false),
                points = points,
                description = "Cobb角测量",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_CA -> {
            val angle = lineAngleDegrees(points[0], points[1])
            AnnotationMeasurement(
                key = measurementKey,
                type = "CA",
                value = formatAngle(angle, signed = true),
                points = points,
                description = "锁骨角测量",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_PELVIC -> {
            val angle = lineAngleDegrees(points[0], points[1])
            AnnotationMeasurement(
                key = measurementKey,
                type = "Pelvic",
                value = formatAngle(angle, signed = true),
                points = points,
                description = "骨盆倾斜角测量",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_SACRAL -> {
            val angle = lineAngleDegrees(points[0], points[1])
            AnnotationMeasurement(
                key = measurementKey,
                type = "Sacral",
                value = formatAngle(angle, signed = true),
                points = points,
                description = "骶骨倾斜角测量",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_TS -> {
            val first = points[0]
            val second = points[1]
            val yRef = (first.y + second.y) / 2.0
            val left = MeasurementPoint(x = first.x, y = yRef)
            val right = MeasurementPoint(x = second.x, y = yRef)
            AnnotationMeasurement(
                key = measurementKey,
                type = "TS",
                value = formatDistanceValue(abs(second.x - first.x), calibration),
                points = listOf(left, right),
                description = "躯干偏移测量",
                kind = AnnotationMeasurementKind.COMPUTED,
                helperSegments = listOf(
                    AnnotationHelperSegment(
                        start = first,
                        end = MeasurementPoint(x = first.x, y = yRef),
                        dashed = true,
                    ),
                    AnnotationHelperSegment(
                        start = second,
                        end = MeasurementPoint(x = second.x, y = yRef),
                        dashed = true,
                    ),
                ),
            )
        }

        TOOL_AVT -> {
            val first = points[0]
            val second = points[1]
            val yRef = (first.y + second.y) / 2.0
            val left = MeasurementPoint(x = first.x, y = yRef)
            val right = MeasurementPoint(x = second.x, y = yRef)
            AnnotationMeasurement(
                key = measurementKey,
                type = "AVT",
                value = formatDistanceValue(abs(second.x - first.x), calibration),
                points = listOf(left, right),
                description = "顶椎偏移测量",
                kind = AnnotationMeasurementKind.COMPUTED,
                helperSegments = listOf(
                    AnnotationHelperSegment(
                        start = first,
                        end = MeasurementPoint(x = first.x, y = yRef),
                        dashed = true,
                    ),
                    AnnotationHelperSegment(
                        start = second,
                        end = MeasurementPoint(x = second.x, y = yRef),
                        dashed = true,
                    ),
                ),
            )
        }

        TOOL_STANDARD_DISTANCE -> {
            val mm = standardDistanceMm ?: DEFAULT_STANDARD_DISTANCE_MM
            AnnotationMeasurement(
                key = measurementKey,
                type = "标准距离",
                value = "${formatStandardDistanceInput(mm)}mm",
                points = points,
                description = "标准距离校准线",
                kind = AnnotationMeasurementKind.COMPUTED,
                panelVisible = false,
            )
        }

        TOOL_VERTEBRA_CENTER -> {
            val center = MeasurementPoint(
                x = points.map { it.x }.average(),
                y = points.map { it.y }.average(),
            )
            AnnotationMeasurement(
                key = measurementKey,
                type = "椎体中心",
                value = formatPointValue(center),
                points = listOf(center),
                description = "椎体中心标注",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_DISTANCE -> {
            val distancePx = hypot(points[1].x - points[0].x, points[1].y - points[0].y)
            AnnotationMeasurement(
                key = measurementKey,
                type = "距离标注",
                value = formatDistanceValue(distancePx, calibration),
                points = points,
                description = "距离标注",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_ANGLE -> {
            val angle = angleAtVertex(points[0], points[1], points[2])
            AnnotationMeasurement(
                key = measurementKey,
                type = "角度标注",
                value = formatAngle(angle, signed = false),
                points = points,
                description = "角度标注",
                kind = AnnotationMeasurementKind.COMPUTED,
            )
        }

        TOOL_AUX_CIRCLE -> auxiliaryMeasurement(measurementKey, "Circle", points)
        TOOL_AUX_ELLIPSE -> auxiliaryMeasurement(measurementKey, "Ellipse", points)
        TOOL_AUX_BOX -> auxiliaryMeasurement(measurementKey, "Box", points)
        TOOL_AUX_ARROW -> auxiliaryMeasurement(measurementKey, "Arrow", points)
        TOOL_AUX_POLYGON -> auxiliaryMeasurement(measurementKey, "Polygon", points)
        else -> null
    }
}

private fun auxiliaryMeasurement(
    key: String,
    type: String,
    points: List<MeasurementPoint>,
): AnnotationMeasurement {
    return AnnotationMeasurement(
        key = key,
        type = type,
        value = "辅助图形",
        points = points,
        description = "辅助图形-$type",
        kind = AnnotationMeasurementKind.COMPUTED,
        auxiliary = true,
    )
}

private fun lineAngleDegrees(start: MeasurementPoint, end: MeasurementPoint): Double {
    val raw = atan2(end.y - start.y, end.x - start.x) * 180.0 / PI
    var normalized = raw
    while (normalized <= -90.0) normalized += 180.0
    while (normalized > 90.0) normalized -= 180.0
    return normalized
}

private fun acuteAngle(first: Double, second: Double): Double {
    var diff = abs(first - second)
    while (diff > 180.0) diff -= 180.0
    return if (diff > 90.0) 180.0 - diff else diff
}

private fun angleAtVertex(
    first: MeasurementPoint,
    vertex: MeasurementPoint,
    third: MeasurementPoint,
): Double {
    val ax = first.x - vertex.x
    val ay = first.y - vertex.y
    val bx = third.x - vertex.x
    val by = third.y - vertex.y
    val magA = hypot(ax, ay)
    val magB = hypot(bx, by)
    if (magA == 0.0 || magB == 0.0) return 0.0
    val cos = ((ax * bx) + (ay * by)) / (magA * magB)
    return acos(cos.coerceIn(-1.0, 1.0)) * 180.0 / PI
}

private fun formatAngle(value: Double, signed: Boolean): String {
    val normalized = if (signed) value else abs(value)
    return "${normalized.toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP)}°"
}

private fun formatDistanceValue(
    pixelDistance: Double,
    calibration: AnnotationCalibrationContext,
): String {
    val distance = if (calibration.standardDistanceMm != null && calibration.standardDistancePoints.size >= 2) {
        val first = calibration.standardDistancePoints[0]
        val second = calibration.standardDistancePoints[1]
        val standardPixelDistance = hypot(second.x - first.x, second.y - first.y)
        if (standardPixelDistance > 0.0) {
            pixelDistance / standardPixelDistance * calibration.standardDistanceMm
        } else {
            pixelDistance
        }
    } else {
        pixelDistance
    }
    return "${distance.toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP)}mm"
}

private fun formatPointValue(point: MeasurementPoint): String {
    val x = point.x.toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP)
    val y = point.y.toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP)
    return "($x, $y)"
}

private fun formatStandardDistanceInput(value: Double): String {
    val rounded = value.toBigDecimal().setScale(2, java.math.RoundingMode.HALF_UP)
    return rounded.stripTrailingZeros().toPlainString()
}
