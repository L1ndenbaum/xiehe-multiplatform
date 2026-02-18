package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun SpineAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SpineTheme.colors.primaryMuted),
        contentAlignment = Alignment.Center,
    ) {
        SpineText(
            text = avatarInitials(name),
            style = SpineTheme.typography.subhead.copy(
                color = SpineTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private fun avatarInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isBlank()) {
        return "?"
    }

    val tokens = trimmed.split(' ').filter { it.isNotBlank() }
    if (tokens.size >= 2) {
        val left = tokens.first().firstOrNull()?.uppercaseChar() ?: return "?"
        val right = tokens.last().firstOrNull()?.uppercaseChar() ?: return left.toString()
        return "$left$right"
    }

    val compact = trimmed.filterNot { it.isWhitespace() }
    return when {
        compact.length >= 2 -> compact.takeLast(2)
        compact.isNotEmpty() -> compact
        else -> "?"
    }
}
