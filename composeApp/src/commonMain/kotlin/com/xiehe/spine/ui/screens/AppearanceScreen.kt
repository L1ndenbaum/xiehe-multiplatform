package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.SelectablePill
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.theme.AppThemeBrandColor
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.viewmodel.AppearanceViewModel

@Composable
fun AppearanceScreen(vm: AppearanceViewModel) {
    val preference by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text = "主色体系", style = SpineTheme.typography.title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectablePill(
                    text = "绿色",
                    selected = preference.brand == AppThemeBrandColor.GREEN,
                    onClick = { vm.updateBrand(AppThemeBrandColor.GREEN) },
                )
                SelectablePill(
                    text = "蓝色",
                    selected = preference.brand == AppThemeBrandColor.BLUE,
                    onClick = { vm.updateBrand(AppThemeBrandColor.BLUE) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(text = "深浅模式", style = SpineTheme.typography.title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectablePill(
                    text = "跟随系统",
                    selected = preference.mode == ThemeMode.SYSTEM,
                    onClick = { vm.updateMode(ThemeMode.SYSTEM) },
                )
                SelectablePill(
                    text = "按时间",
                    selected = preference.mode == ThemeMode.AUTO_TIME,
                    onClick = { vm.updateMode(ThemeMode.AUTO_TIME) },
                )
                SelectablePill(
                    text = "浅色",
                    selected = preference.mode == ThemeMode.LIGHT,
                    onClick = { vm.updateMode(ThemeMode.LIGHT) },
                )
                SelectablePill(
                    text = "深色",
                    selected = preference.mode == ThemeMode.DARK,
                    onClick = { vm.updateMode(ThemeMode.DARK) },
                )
            }
        }
    }
}
