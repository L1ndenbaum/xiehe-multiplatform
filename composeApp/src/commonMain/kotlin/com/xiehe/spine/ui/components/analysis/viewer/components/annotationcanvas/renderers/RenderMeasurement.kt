package com.xiehe.spine.ui.components.analysis.viewer.components.annotationcanvas.renderers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.xiehe.spine.ui.components.analysis.image.AnalysisMeasurementPalette
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurement
import com.xiehe.spine.ui.components.analysis.viewer.AnnotationMeasurementKind
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

fun DrawScope.drawAnnotationMeasurement(
    item: AnnotationMeasurement,
    sx: Float,
    sy: Float,
) {
    val color = AnalysisMeasurementPalette.colorFor(item)
    val points = item.points.map { Offset((it.x * sx).toFloat(), (it.y * sy).toFloat()) }

    item.helperSegments.forEach { segment ->
        drawLine(
            color = if (segment.dashed) AnalysisMeasurementPalette.helperDashedColor else color,
            start = Offset((segment.start.x * sx).toFloat(), (segment.start.y * sy).toFloat()),
            end = Offset((segment.end.x * sx).toFloat(), (segment.end.y * sy).toFloat()),
            strokeWidth = if (segment.dashed) 1.8f else 2.6f,
            pathEffect = if (segment.dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null,
        )
    }

    when (item.type) {
        "Cobb" -> {
            if (points.size >= 4) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                drawLine(color = color, start = points[2], end = points[3], strokeWidth = 3f)
            }
        }

        "角度标注" -> {
            if (points.size >= 3) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
                drawLine(color = color, start = points[1], end = points[2], strokeWidth = 3f)
            }
        }

        "Circle" -> {
            if (points.size >= 2) {
                val center = Offset((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f)
                val radius = hypot(points[1].x - points[0].x, points[1].y - points[0].y) / 2f
                drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 2.6f))
            }
        }

        "Ellipse" -> {
            if (points.size >= 2) {
                val left = minOf(points[0].x, points[1].x)
                val top = minOf(points[0].y, points[1].y)
                val width = kotlin.math.abs(points[1].x - points[0].x)
                val height = kotlin.math.abs(points[1].y - points[0].y)
                drawOval(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.6f),
                )
            }
        }

        "Box" -> {
            if (points.size >= 2) {
                val left = minOf(points[0].x, points[1].x)
                val top = minOf(points[0].y, points[1].y)
                val width = kotlin.math.abs(points[1].x - points[0].x)
                val height = kotlin.math.abs(points[1].y - points[0].y)
                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.6f),
                )
            }
        }

        "Arrow" -> {
            if (points.size >= 2) {
                drawArrow(points[0], points[1], color)
            }
        }

        "Polygon" -> {
            if (points.size >= 2) {
                val path = Path()
                path.moveTo(points[0].x, points[0].y)
                for (index in 1 until points.size) {
                    path.lineTo(points[index].x, points[index].y)
                }
                path.close()
                drawPath(path = path, color = color, style = Stroke(width = 2.6f))
            }
        }

        else -> {
            if (points.size >= 2) {
                drawLine(color = color, start = points[0], end = points[1], strokeWidth = 3f)
            }
        }
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
    if (points.isEmpty()) return Offset.Zero
    if (points.size == 1) {
        return Offset((points[0].x * sx).toFloat(), (points[0].y * sy).toFloat())
    }
    val x = ((points[0].x + points[1].x) / 2.0 * sx).toFloat()
    val y = ((points[0].y + points[1].y) / 2.0 * sy).toFloat()
    return Offset(x, y)
}

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
) {
    drawLine(color = color, start = start, end = end, strokeWidth = 2.8f)
    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowLength = 12f
    val wing = 0.48f
    val p1 = Offset(
        x = end.x - arrowLength * cos(angle - wing),
        y = end.y - arrowLength * sin(angle - wing),
    )
    val p2 = Offset(
        x = end.x - arrowLength * cos(angle + wing),
        y = end.y - arrowLength * sin(angle + wing),
    )
    drawLine(color = color, start = end, end = p1, strokeWidth = 2.8f)
    drawLine(color = color, start = end, end = p2, strokeWidth = 2.8f)
}
