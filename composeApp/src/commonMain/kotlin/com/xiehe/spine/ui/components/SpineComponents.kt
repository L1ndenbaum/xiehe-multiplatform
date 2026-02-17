package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun SpineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SpineTheme.typography.body,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color ?: if (style.color == Color.Unspecified) SpineTheme.colors.textPrimary else style.color),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun SpineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = SpineTheme.colors
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(SpineTheme.radius.md))
            .background(if (enabled) colors.primary else colors.surfaceMuted)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = SpineTheme.typography.body.copy(
                color = if (enabled) colors.onPrimary else colors.textTertiary,
            ),
        )
    }
}

@Composable
fun SpineCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(SpineTheme.radius.lg))
            .background(SpineTheme.colors.surface)
            .border(1.dp, SpineTheme.colors.borderSubtle, RoundedCornerShape(SpineTheme.radius.lg))
            .padding(SpineTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(SpineTheme.spacing.md),
        content = { content() },
    )
}

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

@Composable
fun SpineTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leftText: String? = null,
    rightText: String? = null,
    onLeftClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(colors.primary, colors.primary.copy(alpha = 0.85f)),
                ),
            )
            .padding(horizontal = SpineTheme.spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopActionChip(text = leftText ?: "", onClick = onLeftClick)
        BasicText(
            text = title,
            modifier = Modifier.weight(1f).padding(horizontal = SpineTheme.spacing.base),
            style = SpineTheme.typography.title.copy(color = colors.onPrimary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TopActionChip(text = rightText ?: "", onClick = onRightClick)
    }
}

@Composable
private fun TopActionChip(
    text: String,
    onClick: (() -> Unit)?,
) {
    val colors = SpineTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.onPrimary.copy(alpha = 0.16f))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (text.isNotBlank()) {
            BasicText(
                text = text,
                style = SpineTheme.typography.body.copy(color = colors.onPrimary),
            )
        }
    }
}

@Composable
fun SpineBottomTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(colors.surface)
            .padding(horizontal = SpineTheme.spacing.xl, vertical = SpineTheme.spacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, label ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(SpineTheme.radius.md))
                    .clickable { onSelect(index) }
                    .padding(vertical = SpineTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpineTheme.spacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(SpineTheme.radius.full))
                        .background(if (selectedIndex == index) colors.primary else colors.tabInactive.copy(alpha = 0.35f)),
                )
                BasicText(
                    text = label,
                    style = SpineTheme.typography.caption.copy(
                        color = if (selectedIndex == index) colors.primary else colors.tabInactive,
                    ),
                )
            }
        }
    }
}

@Composable
fun SpineSelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SpineTheme.radius.full))
            .background(if (selected) colors.primaryMuted else colors.surface)
            .border(1.dp, if (selected) colors.primary else colors.borderSubtle, RoundedCornerShape(SpineTheme.radius.full))
            .clickable(onClick = onClick)
            .padding(horizontal = SpineTheme.spacing.base, vertical = SpineTheme.spacing.sm),
    ) {
        BasicText(
            text = text,
            style = SpineTheme.typography.subhead.copy(
                color = if (selected) colors.primary else colors.textSecondary,
            ),
        )
    }
}
