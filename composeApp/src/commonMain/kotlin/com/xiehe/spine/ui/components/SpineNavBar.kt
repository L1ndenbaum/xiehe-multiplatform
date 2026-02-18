package com.xiehe.spine.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class SpineNavItem(
    val label: String,
    val glyph: SpineGlyph,
)

@Composable
fun SpineNavBar(
    items: List<SpineNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SpineBottomTabBar(
        tabs = items.map { it.label },
        icons = items.map { it.glyph },
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
    )
}
