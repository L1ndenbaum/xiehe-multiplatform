package com.xiehe.spine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme
import kotlinx.coroutines.delay

@Composable
fun PickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showActionRow: Boolean = true,
    onConfirm: (() -> Unit)? = null,
    maxDialogWidth: Dp? = null,
    maxDialogHeightFraction: Float? = null,
    overlayMaxAlpha: Float = 0.26f,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(220)
            onDismissRequest()
        }
    }

    val dismiss = {
        visible = false
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (visible) overlayMaxAlpha.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(220),
        label = "picker_overlay_alpha",
    )
    val sizeModifier = Modifier.run {
        var result: Modifier = this
        if (maxDialogWidth != null) {
            result = result.widthIn(max = maxDialogWidth)
        }
        if (maxDialogHeightFraction != null) {
            result = result.fillMaxHeight(maxDialogHeightFraction.coerceIn(0.2f, 0.95f))
        }
        result
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = overlayAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = dismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(220)) + slideInVertically(tween(240)) { it },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 2 },
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .then(sizeModifier)
                    .padding(horizontal = 20.dp)
                    .background(SpineTheme.colors.surface, RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth(),
                        color = SpineTheme.colors.textPrimary,
                    )
                }

                content(dismiss)

                if (showActionRow) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = "取消",
                                style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                                color = SpineTheme.colors.warning,
                                modifier = Modifier.clickable(onClick = dismiss),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(SpineTheme.colors.borderSubtle),
                        )
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = "确定",
                                style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                                color = SpineTheme.colors.warning,
                                modifier = Modifier.clickable {
                                    onConfirm?.invoke()
                                    dismiss()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
