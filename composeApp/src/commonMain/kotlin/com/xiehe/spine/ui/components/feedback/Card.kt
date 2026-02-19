package com.xiehe.spine.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .animateContentSize(tween(220))
            .clip(RoundedCornerShape(SpineTheme.radius.lg))
            .background(SpineTheme.colors.surface)
            .border(1.dp, SpineTheme.colors.borderSubtle, RoundedCornerShape(SpineTheme.radius.lg))
            .padding(SpineTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(SpineTheme.spacing.md),
        content = { content() },
    )
}
