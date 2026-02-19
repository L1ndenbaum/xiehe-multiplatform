package com.xiehe.spine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FilterSelector(
    text: String,
    modifier: Modifier = Modifier,
    leadingGlyph: IconToken,
    onClick: () -> Unit,
) {
    TextField(
        value = text,
        onValueChange = {},
        placeholder = text,
        modifier = modifier.clickable(onClick = onClick),
        readOnly = true,
        leadingGlyph = leadingGlyph,
        trailingGlyph = IconToken.CHEVRON_DOWN,
        onTrailingClick = onClick,
    )
}
