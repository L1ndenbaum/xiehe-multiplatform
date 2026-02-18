package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineNavBar
import com.xiehe.spine.ui.components.SpineNavItem
import com.xiehe.spine.ui.components.SpineTopBar
import com.xiehe.spine.ui.theme.SpineTheme

private val defaultNavItems = listOf(
    SpineNavItem(label = "工作台", glyph = SpineGlyph.DASHBOARD),
    SpineNavItem(label = "患者中心", glyph = SpineGlyph.PATIENTS),
    SpineNavItem(label = "影像中心", glyph = SpineGlyph.IMAGES),
    SpineNavItem(label = "个人中心", glyph = SpineGlyph.PROFILE),
)

@Composable
fun MobileShell(
    title: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBack: (() -> Unit)? = null,
    rightActionGlyph: SpineGlyph? = null,
    onRightAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val navItems = remember { defaultNavItems }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        SpineTopBar(
            title = title,
            modifier = Modifier.statusBarsPadding(),
            leftGlyph = if (onBack != null) SpineGlyph.BACK else null,
            rightGlyph = rightActionGlyph,
            onLeftClick = onBack,
            onRightClick = onRightAction,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        SpineNavBar(
            items = navItems,
            selectedIndex = selectedTab,
            onSelect = onTabSelected,
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}
