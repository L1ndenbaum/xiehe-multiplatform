package com.xiehe.spine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SpineFilterSelector(
    text: String,
    modifier: Modifier = Modifier,
    leadingGlyph: SpineGlyph,
    onClick: () -> Unit,
) {
    SpineTextField(
        value = text,
        onValueChange = {},
        placeholder = text,
        modifier = modifier.clickable(onClick = onClick),
        readOnly = true,
        leadingGlyph = leadingGlyph,
        trailingGlyph = SpineGlyph.CHEVRON_DOWN,
        onTrailingClick = onClick,
    )
}
