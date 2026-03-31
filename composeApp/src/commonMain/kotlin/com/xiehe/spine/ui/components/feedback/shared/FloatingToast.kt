package com.xiehe.spine.ui.components.feedback.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme
import kotlinx.coroutines.delay

@Composable
fun FloatingToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = SpineTheme.colors.primary,
    icon: IconToken = IconToken.BELL,
    durationMillis: Long = 2600L,
) {
    var visible by remember(message) { mutableStateOf(true) }

    LaunchedEffect(message) {
        visible = true
        delay(durationMillis)
        visible = false
        delay(220)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(220)) { -it / 3 },
        exit = fadeOut(animationSpec = tween(220)) +
            slideOutVertically(animationSpec = tween(220)) { -it / 4 },
    ) {
        Row(
            modifier = Modifier
                .shadow(18.dp, RoundedCornerShape(18.dp))
                .background(
                    color = SpineTheme.colors.surface,
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    glyph = icon,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = message,
                style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = SpineTheme.colors.textPrimary,
            )
        }
    }
}
