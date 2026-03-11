package com.xiehe.spine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.theme.AppThemeBrandColor
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.viewmodel.profile.AppearanceViewModel

@Composable
fun AppearanceScreen(vm: AppearanceViewModel) {
    val preference by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text = "主色体系", style = SpineTheme.typography.title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectRowItem(
                    label = "紫色",
                    icon = IconToken.CHECK,
                    selected = preference.brand == AppThemeBrandColor.PURPLE,
                    onClick = { vm.updateBrand(AppThemeBrandColor.PURPLE) },
                    modifier = Modifier.weight(1f),
                )
                SelectRowItem(
                    label = "蓝色",
                    icon = IconToken.MESSAGE,
                    selected = preference.brand == AppThemeBrandColor.BLUE,
                    onClick = { vm.updateBrand(AppThemeBrandColor.BLUE) },
                    modifier = Modifier.weight(1f),
                )
                SelectRowItem(
                    label = "绿色",
                    icon = IconToken.HEART_PULSE,
                    selected = preference.brand == AppThemeBrandColor.GREEN,
                    onClick = { vm.updateBrand(AppThemeBrandColor.GREEN) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text = "深浅模式", style = SpineTheme.typography.title)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeOption(
                    text = "跟随系统",
                    selected = preference.mode == ThemeMode.SYSTEM,
                    onClick = { vm.updateMode(ThemeMode.SYSTEM) },
                )
                ModeOption(
                    text = "按时间",
                    selected = preference.mode == ThemeMode.AUTO_TIME,
                    onClick = { vm.updateMode(ThemeMode.AUTO_TIME) },
                )
                ModeOption(
                    text = "浅色",
                    selected = preference.mode == ThemeMode.LIGHT,
                    onClick = { vm.updateMode(ThemeMode.LIGHT) },
                )
                ModeOption(
                    text = "深色",
                    selected = preference.mode == ThemeMode.DARK,
                    onClick = { vm.updateMode(ThemeMode.DARK) },
                )
            }
        }
    }
}

@Composable
private fun SelectRowItem(
    label: String,
    icon: IconToken,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .background(
                color = if (selected) colors.primary else colors.surfaceMuted,
                shape = RoundedCornerShape(SpineTheme.radius.md),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AppIcon(
            glyph = icon,
            tint = if (selected) colors.onPrimary else colors.textSecondary,
            modifier = Modifier.padding(start = 2.dp),
        )
        Text(
            text = label,
            color = if (selected) colors.onPrimary else colors.textPrimary,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) colors.primary else colors.surfaceMuted,
                RoundedCornerShape(SpineTheme.radius.md),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.body,
            color = if (selected) colors.onPrimary else colors.textPrimary,
        )
        if (selected) {
            Text("✓", color = colors.onPrimary)
        }
    }
}

