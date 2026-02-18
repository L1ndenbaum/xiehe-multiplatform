package com.xiehe.spine.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun SpineBottomTabBar(
    tabs: List<String>,
    icons: List<SpineGlyph>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
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
            val markerWidth by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (selected) 22.dp else 0.dp,
                animationSpec = tween(durationMillis = 180),
                label = "tab_indicator_width",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(SpineTheme.radius.md))
                    .clickable { onSelect(index) }
                    .padding(vertical = SpineTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpineTheme.spacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .width(markerWidth)
                        .height(4.dp)
                        .clip(RoundedCornerShape(SpineTheme.radius.full))
                        .background(if (selected) activeColor else Color.Transparent),
                )
                SpineGlyphIcon(
                    glyph = icons.getOrNull(index) ?: SpineGlyph.DASHBOARD,
                    modifier = Modifier.height(16.dp).width(16.dp),
                    tint = tone,
                )
                BasicText(
                    text = label,
                    style = SpineTheme.typography.caption.copy(color = tone),
                )
            }
        }
    }
}
