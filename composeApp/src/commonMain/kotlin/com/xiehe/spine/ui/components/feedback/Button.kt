package com.xiehe.spine.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingGlyph: IconToken? = null,
    customContainerColor: androidx.compose.ui.graphics.Color? = null,
    customContentColor: androidx.compose.ui.graphics.Color? = null,
) {
    val colors = SpineTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "button_scale",
    )
    Box(
        modifier = modifier
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(SpineTheme.radius.md))
            .background(if (enabled) (customContainerColor ?: colors.primary) else colors.surfaceMuted)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingGlyph != null) {
                AppIcon(
                    glyph = leadingGlyph,
                    modifier = Modifier.size(16.dp),
                    tint = if (enabled) (customContentColor ?: colors.onPrimary) else colors.textTertiary,
                )
            }
            BasicText(
                text = text,
                style = SpineTheme.typography.body.copy(
                    color = if (enabled) (customContentColor ?: colors.onPrimary) else colors.textTertiary,
                ),
            )
        }
    }
}
