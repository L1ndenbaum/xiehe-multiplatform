package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.NavBar
import com.xiehe.spine.ui.components.NavItem
import com.xiehe.spine.ui.components.TopBar
import com.xiehe.spine.ui.theme.SpineTheme

private val defaultNavItems = listOf(
    NavItem(label = "工作台", glyph = IconToken.DASHBOARD),
    NavItem(label = "患者中心", glyph = IconToken.PATIENTS),
    NavItem(label = "影像中心", glyph = IconToken.IMAGES),
    NavItem(label = "个人中心", glyph = IconToken.PROFILE),
)

@Composable
fun MobileShell(
    title: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBack: (() -> Unit)? = null,
    rightActionGlyph: IconToken? = null,
    rightActionText: String? = null,
    onRightAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val navItems = remember { defaultNavItems }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        TopBar(
            title = title,
            modifier = Modifier.statusBarsPadding(),
            leftGlyph = if (onBack != null) IconToken.BACK else null,
            rightGlyph = rightActionGlyph,
            rightText = rightActionText,
            onLeftClick = onBack,
            onRightClick = onRightAction,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpineTheme.colors.surface.copy(alpha = 0.96f))
                .navigationBarsPadding(),
        ) {
            NavBar(
                items = navItems,
                selectedIndex = selectedTab,
                onSelect = onTabSelected,
            )
        }
    }
}
