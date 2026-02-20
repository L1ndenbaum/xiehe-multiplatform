package com.xiehe.spine.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun MeasurementResultsPanel(
    standardDistanceLabel: String,
    expanded: Boolean,
    measurements: List<com.xiehe.spine.ui.viewmodel.ImageAnalysisMeasurement>,
    hiddenKeys: Set<String>,
    onToggleExpanded: () -> Unit,
    onToggleItemVisibility: (String) -> Unit,
    onShowAll: () -> Unit,
    onHideAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Column(
        modifier = modifier
            .width(210.dp)
            .background(
                color = colors.backgroundElevated.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(glyph = IconToken.EYE, tint = colors.textSecondary)
            Text(
                text = "测量结果",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                color = colors.textPrimary,
            )
            Text(
                text = if (expanded) "▴" else "▾",
                style = SpineTheme.typography.body,
                color = colors.textSecondary,
                modifier = Modifier.clickable(onClick = onToggleExpanded),
            )
        }

        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "显示",
                    style = SpineTheme.typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable(onClick = onShowAll),
                )
                Text(
                    text = "隐藏",
                    style = SpineTheme.typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable(onClick = onHideAll),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorTokens.purple.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = standardDistanceLabel,
                    style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = ColorTokens.purple,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(measurements, key = { it.key }) { item ->
                    val hidden = hiddenKeys.contains(item.key)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            glyph = if (hidden) IconToken.EYE_OFF else IconToken.EYE,
                            tint = if (hidden) colors.textTertiary else colors.textSecondary,
                            modifier = Modifier
                                .clickable { onToggleItemVisibility(item.key) },
                        )
                        Text(
                            text = item.type,
                            style = SpineTheme.typography.subhead,
                            color = if (hidden) colors.textTertiary else colors.textPrimary,
                            modifier = Modifier.weight(1f).padding(start = 6.dp),
                            maxLines = 1,
                        )
                        Text(
                            text = item.value,
                            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = if (hidden) colors.textTertiary else ColorTokens.valueYellow,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private object ColorTokens {
    val purple = androidx.compose.ui.graphics.Color(0xFF8A57D9)
    val valueYellow = androidx.compose.ui.graphics.Color(0xFFC9A837)
}
