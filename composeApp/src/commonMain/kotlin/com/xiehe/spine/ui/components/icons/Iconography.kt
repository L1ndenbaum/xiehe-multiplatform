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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import spine.composeapp.generated.resources.Res
import spine.composeapp.generated.resources.icons_action_icon_add
import spine.composeapp.generated.resources.icons_action_icon_back
import spine.composeapp.generated.resources.icons_action_icon_bell
import spine.composeapp.generated.resources.icons_action_icon_calendar
import spine.composeapp.generated.resources.icons_action_icon_chevron_down
import spine.composeapp.generated.resources.icons_action_icon_chevron_right
import spine.composeapp.generated.resources.icons_action_icon_eye
import spine.composeapp.generated.resources.icons_action_icon_eye_off
import spine.composeapp.generated.resources.icons_action_icon_search
import spine.composeapp.generated.resources.icons_navigation_icon_dashboard
import spine.composeapp.generated.resources.icons_navigation_icon_images
import spine.composeapp.generated.resources.icons_navigation_icon_patients
import spine.composeapp.generated.resources.icons_navigation_icon_profile
import spine.composeapp.generated.resources.icons_status_icon_check
import spine.composeapp.generated.resources.icons_status_icon_hourglass
import spine.composeapp.generated.resources.icons_status_icon_image
import spine.composeapp.generated.resources.icons_status_icon_lock
import spine.composeapp.generated.resources.icons_status_icon_message
import spine.composeapp.generated.resources.icons_status_icon_settings
import spine.composeapp.generated.resources.icons_status_icon_users

enum class IconToken {
    DASHBOARD,
    PATIENTS,
    IMAGES,
    MESSAGE,
    PROFILE,
    BACK,
    ADD,
    BELL,
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
}

@Composable
fun AppIcon(
    glyph: IconToken,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.textSecondary,
) {
    val iconVector = vectorResource(glyph.drawable())
    Image(
        painter = rememberVectorPainter(iconVector),
        contentDescription = glyph.name,
        modifier = Modifier.size(18.dp).then(modifier),
        colorFilter = ColorFilter.tint(tint),
    )
}

private fun IconToken.drawable(): DrawableResource {
    return when (this) {
        IconToken.DASHBOARD -> Res.drawable.icons_navigation_icon_dashboard
        IconToken.PATIENTS -> Res.drawable.icons_navigation_icon_patients
        IconToken.IMAGES -> Res.drawable.icons_navigation_icon_images
        IconToken.MESSAGE -> Res.drawable.icons_status_icon_message
        IconToken.PROFILE -> Res.drawable.icons_navigation_icon_profile
        IconToken.BACK -> Res.drawable.icons_action_icon_back
        IconToken.ADD -> Res.drawable.icons_action_icon_add
        IconToken.BELL -> Res.drawable.icons_action_icon_bell
        IconToken.USERS -> Res.drawable.icons_status_icon_users
        IconToken.HOURGLASS -> Res.drawable.icons_status_icon_hourglass
        IconToken.CHECK -> Res.drawable.icons_status_icon_check
        IconToken.IMAGE -> Res.drawable.icons_status_icon_image
        IconToken.SEARCH -> Res.drawable.icons_action_icon_search
        IconToken.CALENDAR -> Res.drawable.icons_action_icon_calendar
        IconToken.LOCK -> Res.drawable.icons_status_icon_lock
        IconToken.SETTINGS -> Res.drawable.icons_status_icon_settings
        IconToken.CHEVRON_DOWN -> Res.drawable.icons_action_icon_chevron_down
        IconToken.CHEVRON_RIGHT -> Res.drawable.icons_action_icon_chevron_right
        IconToken.EYE -> Res.drawable.icons_action_icon_eye
        IconToken.EYE_OFF -> Res.drawable.icons_action_icon_eye_off
    }
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
