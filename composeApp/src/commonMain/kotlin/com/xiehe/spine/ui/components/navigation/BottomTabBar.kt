package com.xiehe.spine.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun BottomTabBar(
    tabs: List<String>,
    icons: List<IconToken>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    var pulseIndex by remember { mutableIntStateOf(-1) }
    var pulseTrigger by remember { mutableIntStateOf(0) }
    val pulseProgress = remember { Animatable(1f) }

    LaunchedEffect(pulseTrigger) {
        if (pulseIndex >= 0) {
            pulseProgress.snapTo(0f)
            pulseProgress.animateTo(1f, animationSpec = tween(durationMillis = 320))
            pulseIndex = -1
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(colors.surface)
            .padding(horizontal = SpineTheme.spacing.xl, vertical = SpineTheme.spacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedIndex == index
            val activeColor = colors.primary
            val mutedColor = colors.tabInactive
            val tone by animateColorAsState(
                targetValue = if (selected) activeColor else mutedColor,
                animationSpec = tween(durationMillis = 180),
                label = "tab_tone",
            )
            val isPulsing = pulseIndex == index
            val progress = if (isPulsing) pulseProgress.value else 1f
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(SpineTheme.radius.md))
                    .clickable {
                        pulseIndex = index
                        pulseTrigger += 1
                        onSelect(index)
                    }
                    .padding(vertical = SpineTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpineTheme.spacing.xs),
            ) {
                Box(
                    modifier = Modifier.width(34.dp).height(26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPulsing) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(22.dp)
                                .graphicsLayer {
                                    val scale = 0.18f + 1.05f * progress
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .alpha(0.30f * (1f - progress))
                                .clip(RoundedCornerShape(SpineTheme.radius.md))
                                .background(colors.textPrimary),
                        )
                    }
                    AppIcon(
                        glyph = icons.getOrNull(index) ?: IconToken.DASHBOARD,
                        modifier = Modifier.height(16.dp).width(16.dp),
                        tint = tone,
                    )
                }
                BasicText(
                    text = label,
                    style = SpineTheme.typography.caption.copy(color = tone),
                )
            }
        }
    }
}
