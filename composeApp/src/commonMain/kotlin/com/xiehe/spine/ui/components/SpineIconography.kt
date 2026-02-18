package com.xiehe.spine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

enum class SpineGlyph {
    DASHBOARD,
    PATIENTS,
    IMAGES,
    PROFILE,
    BACK,
    ADD,
    BELL,
    USERS,
    HOURGLASS,
    CHECK,
    IMAGE,
}

@Composable
fun SpineGlyphIcon(
    glyph: SpineGlyph,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.textSecondary,
) {
    Canvas(modifier = modifier.size(18.dp)) {
        val stroke = Stroke(
            width = size.minDimension * 0.10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        when (glyph) {
            SpineGlyph.DASHBOARD -> {
                val cell = size.minDimension * 0.32f
                val gap = size.minDimension * 0.08f
                val r = CornerRadius(size.minDimension * 0.05f)
                drawRoundRect(tint, Offset(0f, 0f), Size(cell, cell), r, style = stroke)
                drawRoundRect(tint, Offset(cell + gap, 0f), Size(cell, cell), r, style = stroke)
                drawRoundRect(tint, Offset(0f, cell + gap), Size(cell, cell), r, style = stroke)
                drawRoundRect(tint, Offset(cell + gap, cell + gap), Size(cell, cell), r, style = stroke)
            }

            SpineGlyph.PATIENTS, SpineGlyph.USERS -> {
                drawCircle(tint, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.36f, size.height * 0.32f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.68f, size.height * 0.36f), style = stroke)
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 145f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.46f),
                    size = Size(size.width * 0.52f, size.height * 0.42f),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 205f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.48f, size.height * 0.52f),
                    size = Size(size.width * 0.36f, size.height * 0.30f),
                    style = stroke,
                )
            }

            SpineGlyph.IMAGES, SpineGlyph.IMAGE -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.10f, size.height * 0.14f),
                    size = Size(size.width * 0.80f, size.height * 0.72f),
                    cornerRadius = CornerRadius(size.minDimension * 0.10f),
                    style = stroke,
                )
                val mountain = Path().apply {
                    moveTo(size.width * 0.20f, size.height * 0.72f)
                    lineTo(size.width * 0.42f, size.height * 0.48f)
                    lineTo(size.width * 0.56f, size.height * 0.62f)
                    lineTo(size.width * 0.74f, size.height * 0.42f)
                }
                drawPath(mountain, tint, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.30f, size.height * 0.32f), style = stroke)
            }

            SpineGlyph.PROFILE -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.5f, size.height * 0.30f),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.48f),
                    size = Size(size.width * 0.56f, size.height * 0.38f),
                    style = stroke,
                )
            }

            SpineGlyph.BACK -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.72f, size.height * 0.20f),
                    end = Offset(size.width * 0.30f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.72f, size.height * 0.80f),
                    end = Offset(size.width * 0.30f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            SpineGlyph.ADD -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.50f, size.height * 0.20f),
                    end = Offset(size.width * 0.50f, size.height * 0.80f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.20f, size.height * 0.50f),
                    end = Offset(size.width * 0.80f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            SpineGlyph.BELL -> {
                drawArc(
                    color = tint,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.20f, size.height * 0.18f),
                    size = Size(size.width * 0.60f, size.height * 0.56f),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.26f, size.height * 0.62f),
                    end = Offset(size.width * 0.74f, size.height * 0.62f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawCircle(tint, radius = size.minDimension * 0.05f, center = Offset(size.width * 0.5f, size.height * 0.74f))
            }

            SpineGlyph.HOURGLASS -> {
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.18f), Offset(size.width * 0.72f, size.height * 0.18f), stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.82f), Offset(size.width * 0.72f, size.height * 0.82f), stroke.width, cap = StrokeCap.Round)
                val top = Path().apply {
                    moveTo(size.width * 0.30f, size.height * 0.22f)
                    lineTo(size.width * 0.70f, size.height * 0.22f)
                    lineTo(size.width * 0.50f, size.height * 0.48f)
                    close()
                }
                val bottom = Path().apply {
                    moveTo(size.width * 0.30f, size.height * 0.78f)
                    lineTo(size.width * 0.70f, size.height * 0.78f)
                    lineTo(size.width * 0.50f, size.height * 0.52f)
                    close()
                }
                drawPath(top, tint, style = stroke)
                drawPath(bottom, tint, style = stroke)
            }

            SpineGlyph.CHECK -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.20f, size.height * 0.54f),
                    end = Offset(size.width * 0.42f, size.height * 0.76f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.42f, size.height * 0.76f),
                    end = Offset(size.width * 0.82f, size.height * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun SpineMiniBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    accentColor: Color = SpineTheme.colors.primary,
) {
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
            val count = values.size.coerceAtLeast(1)
            val gap = size.width * 0.04f
            val barWidth = (size.width - gap * (count + 1)) / count
            values.forEachIndexed { index, value ->
                val left = gap + index * (barWidth + gap)
                val ratio = (value / max).coerceIn(0f, 1f)
                val barHeight = size.height * (0.2f + ratio * 0.8f)
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.18f),
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth * 0.26f, barWidth * 0.26f),
                )
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(left, size.height - barHeight * 0.72f),
                    size = Size(barWidth, barHeight * 0.72f),
                    cornerRadius = CornerRadius(barWidth * 0.26f, barWidth * 0.26f),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                SpineText(text = label, style = SpineTheme.typography.caption, color = SpineTheme.colors.textTertiary)
            }
        }
    }
}

@Composable
fun SpineProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.primary,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(SpineTheme.radius.full))
            .background(SpineTheme.colors.primaryMuted.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val stroke = size.minDimension * 0.14f
            drawArc(
                color = tint.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke),
            )
            drawArc(
                color = tint,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}
