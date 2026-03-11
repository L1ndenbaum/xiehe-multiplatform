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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

private val shellHeaderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFF6D28D9),
        Color(0xFF5B21B6),
    ),
)

@Composable
fun DashboardShellHeader(
    userName: String,
    primaryMeta: String,
    secondaryMeta: String,
    onMessages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(shellHeaderBrush)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        glyph = IconToken.USER_ROUND,
                        tint = SpineTheme.colors.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "早上好",
                        style = SpineTheme.typography.subhead,
                        color = Color(0xFFDDD6FE),
                    )
                    Text(
                        text = userName,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                        color = SpineTheme.colors.onPrimary,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = primaryMeta,
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        text = secondaryMeta,
                        style = SpineTheme.typography.caption,
                        color = Color(0xFFDDD6FE),
                    )
                }
                HeaderActionBubble(glyph = IconToken.BELL, onClick = onMessages, showBadge = true)
            }
        }
    }
}

@Composable
fun SearchShellHeader(
    title: String,
    subtitle: String,
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    searchPlaceholder: String,
    actionGlyph: IconToken,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(shellHeaderBrush)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = SpineTheme.colors.onPrimary,
                )
                Text(
                    text = subtitle,
                    style = SpineTheme.typography.subhead,
                    color = Color(0xFFDDD6FE),
                )
            }
            HeaderActionBubble(glyph = actionGlyph, onClick = onAction)
        }
        HeaderSearchField(
            value = searchValue,
            onValueChange = onSearchValueChange,
            placeholder = searchPlaceholder,
        )
    }
}

@Composable
fun SimpleShellHeader(
    title: String,
    subtitle: String? = null,
    leadingGlyph: IconToken? = null,
    onLeadingAction: (() -> Unit)? = null,
    actionGlyph: IconToken? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(shellHeaderBrush)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingGlyph != null && onLeadingAction != null) {
            HeaderActionBubble(glyph = leadingGlyph, onClick = onLeadingAction)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                color = SpineTheme.colors.onPrimary,
            )
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = SpineTheme.typography.subhead,
                    color = Color(0xFFDDD6FE),
                )
            }
        }
        if (actionGlyph != null && onAction != null) {
            HeaderActionBubble(glyph = actionGlyph, onClick = onAction)
        }
    }
}

@Composable
private fun HeaderSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val colors = SpineTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(glyph = IconToken.SEARCH, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    style = SpineTheme.typography.body,
                    color = colors.textTertiary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = SpineTheme.typography.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primary),
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
private fun HeaderActionBubble(
    glyph: IconToken,
    onClick: () -> Unit,
    showBadge: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(glyph = glyph, tint = SpineTheme.colors.onPrimary, modifier = Modifier.size(18.dp))
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB7185))
                    .border(1.dp, Color(0xFF6D28D9), CircleShape),
            )
        }
    }
}