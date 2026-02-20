package com.xiehe.spine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

enum class IconToken {
    DASHBOARD,
    PATIENTS,
    IMAGES,
    MESSAGE,
    PROFILE,
    BACK,
    ADD,
    BELL,
    SAVE,
    IMPORT,
    EXPORT,
    MINUS,
    USERS,
    HOURGLASS,
    CHECK,
    IMAGE,
    SEARCH,
    CALENDAR,
    LOCK,
    SETTINGS,
    CHEVRON_DOWN,
    CHEVRON_RIGHT,
    EYE,
    EYE_OFF,
    DOWNLOAD,
    DELETE,
    AI_DETECT,
    AI_MEASURE,
    REPORT,
    MEASURE_TOOLKIT,
    MEASURE_MOVE,
    MEASURE_T1_TILT,
    MEASURE_COBB,
    MEASURE_CA,
    MEASURE_PELVIC,
    MEASURE_TS,
    MEASURE_AVT,
    MEASURE_STANDARD_DISTANCE,
}

@Composable
fun AppIcon(
    glyph: IconToken,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.textSecondary,
) {
    Image(
        painter = platformIconPainter(glyph),
        contentDescription = glyph.name,
        modifier = Modifier.size(18.dp).then(modifier),
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
fun MiniBarChart(
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
                Text(text = label, style = SpineTheme.typography.caption, color = SpineTheme.colors.textTertiary)
            }
        }
    }
}

@Composable
fun ProgressRing(
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
        Canvas(modifier = Modifier.fillMaxSize(0.72f)) {
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
                style = Stroke(stroke),
            )
        }
    }
}
