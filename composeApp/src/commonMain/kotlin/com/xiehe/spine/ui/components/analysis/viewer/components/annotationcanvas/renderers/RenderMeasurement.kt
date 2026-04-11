package com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.xiehe.spine.ui.components.analysis.image.AnalysisMeasurementPalette
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

private data class PelvicGeometry(
    val femoralHeadCenter: Offset?,
    val sacralLeft: Offset,
    val sacralRight: Offset,
    val sacralMidpoint: Offset,
    val sacralNormal: Offset,
)

fun DrawScope.drawAnnotationMeasurement(
    item: AnnotationMeasurement,
    sx: Float,
    sy: Float,
) {
    val color = AnalysisMeasurementPalette.colorFor(item)
    val points = item.points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }

    item.helperSegments.forEach { segment ->
        drawDashedSegment(
            start = Offset((segment.start.x * sx).toFloat(), (segment.start.y * sy).toFloat()),
            end = Offset((segment.end.x * sx).toFloat(), (segment.end.y * sy).toFloat()),
            color = if (segment.dashed) AnalysisMeasurementPalette.helperDashedColor else color,
            strokeWidth = if (segment.dashed) 1.8f else 2.6f,
            dashed = segment.dashed,
        )
    }

    when (item.type) {
        "T1 Tilt",
        "T1 Slope",
        -> drawLineWithHorizontalAndArc(points, color)

        "Cobb",
        "C2-C7 CL",
        "TK T2-T5",
        "TK T5-T12",
        "T10-L2",
        "LL L1-S1",
        "LL L1-L4",
        "LL L4-S1",
        -> drawTwoDashedLines(points, color)

        "CA",
        "Pelvic",
        -> drawSingleLineWithHorizontal(points, color)

        "Sacral" -> drawSacralWithPerpendicular(points, color)
        "SS" -> drawSS(points, color)
        "AVT" -> drawVerticalGuideLines(points, color)
        "TTS" -> drawTts(points, color)
        "LLD" -> drawHorizontalGuideLines(points, color)
        "TS(Trunk Shift)" -> drawC7Offset(points, color)
        "TPA" -> drawTpa(points, color)
        "SVA" -> drawSva(points, color)
        "PI" -> drawPi(points, color)
        "PT" -> drawPt(points, color)
        "角度测量" -> drawThreePointAngle(points, color)
        "距离标注",
        "长度测量",
        -> drawSimpleLine(points, color)

        "角度标注" -> drawTwoDashedLines(points, color)
        "辅助水平线" -> drawSingleHorizontalLine(points, color)
        "辅助垂直线" -> drawSingleVerticalLine(points, color)
        "Auxiliary Circle" -> drawCircleShape(points, color)
        "Auxiliary Ellipse" -> drawEllipseShape(points, color)
        "Auxiliary Box" -> drawBoxShape(points, color)
        "Arrow" -> drawArrowShape(points, color)
        "Polygons" -> drawPolygonShape(points, color)
        "椎体中心" -> drawVertebraCenter(points, color)
        else -> drawSimpleLine(points, color)
    }

    points.forEach { point ->
        drawCircle(
            color = color,
            radius = if (item.kind == AnnotationMeasurementKind.DETECTED) 4.5f else 5f,
            center = point,
        )
    }
}

fun AnnotationMeasurement.tagAnchor(
    sx: Float,
    sy: Float,
): Offset {
    val points = points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }
    if (points.isEmpty()) return Offset.Zero

    return when (type) {
        "T1 Tilt",
        "T1 Slope",
        "CA",
        "Pelvic",
        "Sacral",
        "SS",
        "距离标注",
        "长度测量",
        "辅助水平线",
        "辅助垂直线",
        -> midpoint(points[0], points[points.lastIndex]).copy(
            y = minOf(points[0].y, points[points.lastIndex].y) - 20f,
        )

        "Cobb",
        "C2-C7 CL",
        "TK T2-T5",
        "TK T5-T12",
        "T10-L2",
        "LL L1-S1",
        "LL L1-L4",
        "LL L4-S1",
        "角度标注",
        -> average(points).copy(y = points.minOf { it.y } - 24f)

        "AVT",
        "TTS",
        "TS(Trunk Shift)",
        "SVA",
        -> average(points).copy(y = points.minOf { it.y } - 18f)

        "PI",
        "PT",
        "TPA",
        -> average(points)

        "椎体中心" -> average(points)
        else -> average(points)
    }
}

private fun DrawScope.drawLineWithHorizontalAndArc(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
    drawDashedSegment(
        start = Offset(points[0].x - 100f, points[0].y),
        end = Offset(points[0].x + 100f, points[0].y),
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
    val angle = atan2(points[1].y - points[0].y, points[1].x - points[0].x) * (180f / PI.toFloat())
    val normalized = normalizeAngleToOrthogonalRange(angle)
    drawAngleArc(
        vertex = points[0],
        startAngleDegrees = 0f,
        endAngleDegrees = normalized,
        radius = 30f,
        color = color,
    )
}

private fun DrawScope.drawSingleLineWithHorizontal(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
    drawDashedSegment(
        start = Offset(points[0].x - 100f, points[0].y),
        end = Offset(points[0].x + 100f, points[0].y),
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
}

private fun DrawScope.drawTwoDashedLines(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 4) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
    drawLine(color = color, start = points[2], end = points[3], strokeWidth = 2.4f)

    val primaryGuide = extendGuide(points[0], points[1])
    val secondaryGuide = extendGuide(points[2], points[3])
    drawDashedSegment(
        start = primaryGuide.first,
        end = primaryGuide.second,
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
    drawDashedSegment(
        start = secondaryGuide.first,
        end = secondaryGuide.second,
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
}

private fun DrawScope.drawSacralWithPerpendicular(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
    val mid = midpoint(points[0], points[1])
    drawDashedSegment(
        start = Offset(mid.x, mid.y - 800f),
        end = Offset(mid.x, mid.y + 800f),
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
}

private fun DrawScope.drawSS(
    points: List<Offset>,
    color: Color,
) {
    val geometry = pelvicGeometry(points) ?: return
    drawLine(color = color, start = geometry.sacralLeft, end = geometry.sacralRight, strokeWidth = 2.4f)
    drawDashedSegment(
        start = Offset(geometry.sacralLeft.x - 100f, geometry.sacralLeft.y),
        end = Offset(geometry.sacralLeft.x + 100f, geometry.sacralLeft.y),
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
    val normalLength = 80f
    drawDashedSegment(
        start = Offset(
            geometry.sacralMidpoint.x - geometry.sacralNormal.x * normalLength,
            geometry.sacralMidpoint.y - geometry.sacralNormal.y * normalLength,
        ),
        end = Offset(
            geometry.sacralMidpoint.x + geometry.sacralNormal.x * normalLength,
            geometry.sacralMidpoint.y + geometry.sacralNormal.y * normalLength,
        ),
        color = color,
        strokeWidth = 1.5f,
        dashed = true,
    )
    drawCircle(color = color, radius = 3f, center = geometry.sacralMidpoint)
}

private fun DrawScope.drawPi(
    points: List<Offset>,
    color: Color,
) {
    val geometry = pelvicGeometry(points) ?: return
    val normalLength = 80f
    drawLine(color = color, start = geometry.sacralLeft, end = geometry.sacralRight, strokeWidth = 2.4f)
    drawDashedSegment(
        start = Offset(
            geometry.sacralMidpoint.x - geometry.sacralNormal.x * normalLength,
            geometry.sacralMidpoint.y - geometry.sacralNormal.y * normalLength,
        ),
        end = Offset(
            geometry.sacralMidpoint.x + geometry.sacralNormal.x * normalLength,
            geometry.sacralMidpoint.y + geometry.sacralNormal.y * normalLength,
        ),
        color = color,
        strokeWidth = 1.5f,
        dashed = true,
    )
    drawCircle(color = color, radius = 3f, center = geometry.sacralMidpoint)
    val femoral = geometry.femoralHeadCenter ?: return
    drawDashedSegment(
        start = femoral,
        end = geometry.sacralMidpoint,
        color = color,
        strokeWidth = 1.8f,
        dashed = true,
    )
    val femoralAngle = angleDegrees(geometry.sacralMidpoint, femoral)
    val normalAngle = closestAngle(
        femoralAngle,
        listOf(
            angleDegrees(Offset.Zero, geometry.sacralNormal),
            angleDegrees(Offset.Zero, Offset(-geometry.sacralNormal.x, -geometry.sacralNormal.y)),
        ),
    )
    val radius = pelvicArcRadius(femoral, geometry.sacralMidpoint, normalLength, inner = false)
    drawAngleArc(geometry.sacralMidpoint, normalAngle, femoralAngle, radius, color)
}

private fun DrawScope.drawPt(
    points: List<Offset>,
    color: Color,
) {
    val geometry = pelvicGeometry(points) ?: return
    drawLine(color = color, start = geometry.sacralLeft, end = geometry.sacralRight, strokeWidth = 2.4f)
    drawCircle(color = color, radius = 3f, center = geometry.sacralMidpoint)
    drawDashedSegment(
        start = Offset(geometry.sacralMidpoint.x, geometry.sacralMidpoint.y - 80f),
        end = Offset(geometry.sacralMidpoint.x, geometry.sacralMidpoint.y + 80f),
        color = AnalysisMeasurementPalette.helperDashedColor,
        strokeWidth = 1.4f,
        dashed = true,
    )
    val femoral = geometry.femoralHeadCenter ?: return
    drawDashedSegment(
        start = femoral,
        end = geometry.sacralMidpoint,
        color = color,
        strokeWidth = 1.8f,
        dashed = true,
    )
    val femoralAngle = angleDegrees(geometry.sacralMidpoint, femoral)
    val verticalAngle = closestAngle(femoralAngle, listOf(-90f, 90f))
    val radius = pelvicArcRadius(femoral, geometry.sacralMidpoint, 80f, inner = true)
    drawAngleArc(geometry.sacralMidpoint, verticalAngle, femoralAngle, radius, color)
}

private fun DrawScope.drawVerticalGuideLines(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    val height = 150f
    points.take(2).forEach { point ->
        drawDashedSegment(
            start = Offset(point.x, point.y - height / 2f),
            end = Offset(point.x, point.y + height / 2f),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
    }
}

private fun DrawScope.drawTts(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    val trunkMid = midpoint(points[0], points[1])
    val height = 150f
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
    drawDashedSegment(
        start = Offset(trunkMid.x, trunkMid.y - height / 2f),
        end = Offset(trunkMid.x, trunkMid.y + height / 2f),
        color = color,
        strokeWidth = 2f,
        dashed = true,
    )
    if (points.size < 4) return
    val sacralMid = midpoint(points[2], points[3])
    drawLine(color = color.copy(alpha = 0.6f), start = points[2], end = points[3], strokeWidth = 2.2f)
    drawDashedSegment(
        start = Offset(sacralMid.x, sacralMid.y - height / 2f),
        end = Offset(sacralMid.x, sacralMid.y + height / 2f),
        color = color.copy(alpha = 0.7f),
        strokeWidth = 2f,
        dashed = true,
    )
    val connectorY = (trunkMid.y + sacralMid.y) / 2f
    drawLine(color = color, start = Offset(trunkMid.x, connectorY), end = Offset(sacralMid.x, connectorY), strokeWidth = 1.6f)
    drawArrowHead(Offset(minOf(trunkMid.x, sacralMid.x), connectorY), color, pointingRight = true)
    drawArrowHead(Offset(maxOf(trunkMid.x, sacralMid.x), connectorY), color, pointingRight = false)
}

private fun DrawScope.drawHorizontalGuideLines(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    val width = 150f
    points.take(2).forEach { point ->
        drawDashedSegment(
            start = Offset(point.x - width / 2f, point.y),
            end = Offset(point.x + width / 2f, point.y),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
    }
}

private fun DrawScope.drawC7Offset(
    points: List<Offset>,
    color: Color,
) {
    if (points.isEmpty()) return
    val height = 150f
    if (points.size >= 4) {
        drawQuadrilateral(points.take(4), color.copy(alpha = 0.45f))
        val center = average(points.take(4))
        drawCircle(color = color, radius = 3f, center = center)
        drawDashedSegment(
            start = Offset(center.x, center.y - height / 2f),
            end = Offset(center.x, center.y + height / 2f),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
    }
    if (points.size >= 6) {
        val referenceMid = midpoint(points[4], points[5])
        drawDashedSegment(points[4], points[5], color.copy(alpha = 0.5f), 1.4f, true)
        drawCircle(color = color, radius = 3f, center = referenceMid)
        drawDashedSegment(
            start = Offset(referenceMid.x, referenceMid.y - height / 2f),
            end = Offset(referenceMid.x, referenceMid.y + height / 2f),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
    }
}

private fun DrawScope.drawTpa(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 7) return
    drawQuadrilateral(points.take(4), color.copy(alpha = 0.35f))
    val center = average(points.take(4))
    val vertex = points[4]
    val tailMid = midpoint(points[5], points[6])
    drawDashedSegment(center, vertex, color, 2f, true)
    drawDashedSegment(vertex, tailMid, color, 2f, true)
    drawDashedSegment(points[5], points[6], color.copy(alpha = 0.5f), 1.4f, true)
    val firstAngle = angleDegrees(vertex, center)
    val secondAngle = angleDegrees(vertex, tailMid)
    drawAngleArc(vertex, firstAngle, secondAngle, 40f, color)
}

private fun DrawScope.drawSva(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 5) return
    drawQuadrilateral(points.take(4), color.copy(alpha = 0.35f))
    val center = average(points.take(4))
    val reference = points[4]
    drawDashedSegment(
        start = Offset(center.x, center.y - 75f),
        end = Offset(center.x, center.y + 75f),
        color = color,
        strokeWidth = 2f,
        dashed = true,
    )
    drawCircle(color = color, radius = 3f, center = center)
    drawDashedSegment(
        start = Offset(reference.x, reference.y - 75f),
        end = Offset(reference.x, reference.y + 75f),
        color = color,
        strokeWidth = 2f,
        dashed = true,
    )
}

private fun DrawScope.drawThreePointAngle(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 3) return
    drawLine(color = color, start = points[1], end = points[0], strokeWidth = 2.4f)
    drawLine(color = color, start = points[1], end = points[2], strokeWidth = 2.4f)
    val firstAngle = angleDegrees(points[1], points[0])
    val secondAngle = angleDegrees(points[1], points[2])
    drawAngleArc(points[1], firstAngle, secondAngle, 24f, color)
}

private fun DrawScope.drawSimpleLine(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.4f)
}

private fun DrawScope.drawSingleHorizontalLine(
    points: List<Offset>,
    color: Color,
) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        drawDashedSegment(
            start = Offset(points[0].x - 40f, points[0].y),
            end = Offset(points[0].x + 40f, points[0].y),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
        return
    }
    drawDashedSegment(points[0], points[1], color, 2f, true)
}

private fun DrawScope.drawSingleVerticalLine(
    points: List<Offset>,
    color: Color,
) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        drawDashedSegment(
            start = Offset(points[0].x, points[0].y - 40f),
            end = Offset(points[0].x, points[0].y + 40f),
            color = color,
            strokeWidth = 2f,
            dashed = true,
        )
        return
    }
    drawDashedSegment(points[0], points[1], color, 2f, true)
}

private fun DrawScope.drawCircleShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val radius = hypot(points[1].x - points[0].x, points[1].y - points[0].y)
    drawCircle(color = color, radius = radius, center = points[0], style = Stroke(width = 2.4f))
}

private fun DrawScope.drawEllipseShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    drawOval(
        color = color,
        topLeft = Offset(points[0].x - abs(points[1].x - points[0].x), points[0].y - abs(points[1].y - points[0].y)),
        size = Size(abs(points[1].x - points[0].x) * 2f, abs(points[1].y - points[0].y) * 2f),
        style = Stroke(width = 2.4f),
    )
}

private fun DrawScope.drawBoxShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    drawRect(
        color = color,
        topLeft = Offset(minOf(points[0].x, points[1].x), minOf(points[0].y, points[1].y)),
        size = Size(abs(points[1].x - points[0].x), abs(points[1].y - points[0].y)),
        style = Stroke(width = 2.4f),
    )
}

private fun DrawScope.drawArrowShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    drawLine(color = color, start = points[0], end = points[1], strokeWidth = 2.6f)
    val angle = atan2(points[1].y - points[0].y, points[1].x - points[0].x)
    val arrowLength = 12f
    val wing = 0.48f
    val p1 = Offset(
        x = points[1].x - arrowLength * cos(angle - wing),
        y = points[1].y - arrowLength * sin(angle - wing),
    )
    val p2 = Offset(
        x = points[1].x - arrowLength * cos(angle + wing),
        y = points[1].y - arrowLength * sin(angle + wing),
    )
    drawLine(color = color, start = points[1], end = p1, strokeWidth = 2.6f)
    drawLine(color = color, start = points[1], end = p2, strokeWidth = 2.6f)
}

private fun DrawScope.drawPolygonShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (index in 1 until points.size) {
            lineTo(points[index].x, points[index].y)
        }
        close()
    }
    drawPath(path = path, color = color, style = Stroke(width = 2.4f))
}

private fun DrawScope.drawVertebraCenter(points: List<Offset>, color: Color) {
    if (points.size < 4) return
    drawQuadrilateral(points.take(4), color)
    drawCircle(color = color, radius = 4f, center = average(points.take(4)))
}

private fun DrawScope.drawQuadrilateral(points: List<Offset>, color: Color) {
    if (points.size < 4) return
    repeat(4) { index ->
        val start = points[index]
        val end = points[(index + 1) % 4]
        drawDashedSegment(start, end, color, 1.4f, true)
    }
}

private fun DrawScope.drawAngleArc(
    vertex: Offset,
    startAngleDegrees: Float,
    endAngleDegrees: Float,
    radius: Float,
    color: Color,
) {
    val sweep = shortestSweep(startAngleDegrees, endAngleDegrees)
    if (abs(sweep) < 1f) return
    val path = Path().apply {
        arcTo(
            rect = Rect(
                left = vertex.x - radius,
                top = vertex.y - radius,
                right = vertex.x + radius,
                bottom = vertex.y + radius,
            ),
            startAngleDegrees = startAngleDegrees,
            sweepAngleDegrees = sweep,
            forceMoveTo = true,
        )
    }
    drawPath(path = path, color = color, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawArrowHead(
    point: Offset,
    color: Color,
    pointingRight: Boolean,
) {
    val direction = if (pointingRight) 1f else -1f
    val path = Path().apply {
        moveTo(point.x, point.y)
        lineTo(point.x + 6f * direction, point.y - 4f)
        lineTo(point.x + 6f * direction, point.y + 4f)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

private fun DrawScope.drawDashedSegment(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    dashed: Boolean,
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null,
    )
}

private fun pelvicGeometry(points: List<Offset>): PelvicGeometry? {
    if (points.size < 2) return null
    val femoral = if (points.size >= 3) points[0] else null
    val sacralLeft = if (points.size >= 3) points[1] else points[0]
    val sacralRight = if (points.size >= 3) points[2] else points[1]
    val dx = sacralRight.x - sacralLeft.x
    val dy = sacralRight.y - sacralLeft.y
    val length = sqrt(dx * dx + dy * dy)
    if (length == 0f) return null
    return PelvicGeometry(
        femoralHeadCenter = femoral,
        sacralLeft = sacralLeft,
        sacralRight = sacralRight,
        sacralMidpoint = midpoint(sacralLeft, sacralRight),
        sacralNormal = Offset(-dy / length, dx / length),
    )
}

private fun normalizeAngleToOrthogonalRange(angle: Float): Float {
    var normalized = angle
    while (normalized > 90f) normalized -= 180f
    while (normalized < -90f) normalized += 180f
    return normalized
}

private fun shortestSweep(start: Float, end: Float): Float {
    var diff = (end - start) % 360f
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f
    return diff
}

private fun angleDegrees(origin: Offset, target: Offset): Float {
    return (atan2(target.y - origin.y, target.x - origin.x) * (180f / PI.toFloat()))
}

private fun closestAngle(base: Float, candidates: List<Float>): Float {
    return candidates.minByOrNull { abs(shortestSweep(base, it)) } ?: base
}

private fun pelvicArcRadius(
    first: Offset,
    second: Offset,
    guideLength: Float,
    inner: Boolean,
): Float {
    val referenceLength = hypot(first.x - second.x, first.y - second.y)
    val baseRadius = maxOf(12f, minOf(36f, referenceLength * 0.35f, guideLength * 0.45f))
    return if (inner) maxOf(9f, baseRadius - 10f) else baseRadius
}

private fun midpoint(first: Offset, second: Offset): Offset {
    return Offset((first.x + second.x) / 2f, (first.y + second.y) / 2f)
}

private fun average(points: List<Offset>): Offset {
    return Offset(
        x = points.map { it.x }.average().toFloat(),
        y = points.map { it.y }.average().toFloat(),
    )
}

private fun extendGuide(
    start: Offset,
    end: Offset,
    distance: Float = 120f,
): Pair<Offset, Offset> {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = hypot(dx, dy)
    if (length <= 0f) return start to end
    val ux = dx / length
    val uy = dy / length
    return Offset(start.x - ux * distance, start.y - uy * distance) to
        Offset(end.x + ux * distance, end.y + uy * distance)
}
