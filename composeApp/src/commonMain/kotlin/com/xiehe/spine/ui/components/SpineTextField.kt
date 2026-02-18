package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
) {
    val colors = SpineTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SpineTheme.radius.md))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(SpineTheme.radius.md))
            .padding(horizontal = SpineTheme.spacing.xl, vertical = SpineTheme.spacing.lg),
    ) {
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
        )
    }
}
