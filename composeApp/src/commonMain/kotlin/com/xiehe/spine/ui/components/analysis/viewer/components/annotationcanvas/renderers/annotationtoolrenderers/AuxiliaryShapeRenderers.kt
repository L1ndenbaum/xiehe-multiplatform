package com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers.annotationtoolrenderers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers.shared.average
import com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers.shared.drawQuadrilateral
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal fun DrawScope.drawCircleShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val radius = hypot(points[1].x - points[0].x, points[1].y - points[0].y)
    drawCircle(color = color, radius = radius, center = points[0], style = Stroke(width = 2.4f))
}

internal fun DrawScope.drawEllipseShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    drawOval(
        color = color,
        topLeft = Offset(
            points[0].x - abs(points[1].x - points[0].x),
            points[0].y - abs(points[1].y - points[0].y),
        ),
        size = Size(abs(points[1].x - points[0].x) * 2f, abs(points[1].y - points[0].y) * 2f),
        style = Stroke(width = 2.4f),
    )
}

internal fun DrawScope.drawBoxShape(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    drawRect(
        color = color,
        topLeft = Offset(minOf(points[0].x, points[1].x), minOf(points[0].y, points[1].y)),
        size = Size(abs(points[1].x - points[0].x), abs(points[1].y - points[0].y)),
        style = Stroke(width = 2.4f),
    )
}

internal fun DrawScope.drawArrowShape(points: List<Offset>, color: Color) {
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

internal fun DrawScope.drawPolygonShape(points: List<Offset>, color: Color) {
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

internal fun DrawScope.drawVertebraCenter(points: List<Offset>, color: Color) {
    if (points.size < 4) return
    drawQuadrilateral(points.take(4), color)
    drawCircle(color = color, radius = 4f, center = average(points.take(4)))
}
