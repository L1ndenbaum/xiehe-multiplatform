package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun SpineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    leadingGlyph: SpineGlyph? = null,
    trailingGlyph: SpineGlyph? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    val colors = SpineTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(SpineTheme.radius.md))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(SpineTheme.radius.md))
            .padding(horizontal = SpineTheme.spacing.xl, vertical = SpineTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingGlyph != null) {
            SpineGlyphIcon(
                glyph = leadingGlyph,
                modifier = Modifier.size(16.dp).padding(end = 6.dp),
                tint = colors.textTertiary,
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                BasicText(
                    text = placeholder,
                    style = SpineTheme.typography.body.copy(color = colors.textTertiary),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = SpineTheme.typography.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primary),
                interactionSource = interactionSource,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                readOnly = readOnly,
            )
        }
        if (trailingGlyph != null) {
            SpineGlyphIcon(
                glyph = trailingGlyph,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 6.dp)
                    .then(
                        if (onTrailingClick != null) {
                            Modifier.clickable(onClick = onTrailingClick)
                        } else {
                            Modifier
                        },
                    ),
                tint = colors.textTertiary,
            )
        }
    }
}
