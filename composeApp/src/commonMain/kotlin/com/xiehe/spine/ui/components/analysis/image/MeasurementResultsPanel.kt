package com.xiehe.spine.ui.components.analysis.image

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.image.ImageAnalysisMeasurement

@Composable
fun MeasurementResultsPanel(
    standardDistanceLabel: String,
    expanded: Boolean,
    computedMeasurements: List<ImageAnalysisMeasurement>,
    detectedPoseFields: List<ImageAnalysisMeasurement>,
    hiddenKeys: Set<String>,
    onToggleExpanded: () -> Unit,
    onToggleItemVisibility: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onShowAll: () -> Unit,
    onHideAll: () -> Unit,
    onShowComputed: () -> Unit,
    onHideComputed: () -> Unit,
    onShowDetected: () -> Unit,
    onHideDetected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    val hasHiddenItems = hiddenKeys.isNotEmpty()
    val computedKeys = computedMeasurements.map { it.key }
    val detectedKeys = detectedPoseFields.map { it.key }
    val hasComputedItems = computedKeys.isNotEmpty()
    val hasDetectedItems = detectedKeys.isNotEmpty()
    val allComputedHidden = hasComputedItems && computedKeys.all(hiddenKeys::contains)
    val allDetectedHidden = hasDetectedItems && detectedKeys.all(hiddenKeys::contains)
    val chevronRotation = animateFloatAsState(
        targetValue = if (expanded) -90f else 90f,
        animationSpec = tween(durationMillis = 180),
        label = "analysis_results_collapse_rotation",
    )

    Column(
        modifier = modifier
            .width(178.dp)
            .background(
                color = colors.backgroundElevated.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                glyph = if (hasHiddenItems) IconToken.EYE_OFF else IconToken.EYE,
                tint = colors.textSecondary,
                modifier = Modifier.clickable {
                    if (hasHiddenItems) onShowAll() else onHideAll()
                },
            )
            Text(
                text = "测量结果",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                color = colors.textPrimary,
            )
            AppIcon(
                glyph = IconToken.BACK,
                tint = colors.textSecondary,
                modifier = Modifier
                    .graphicsLayer { rotationZ = chevronRotation.value }
                    .clickable(onClick = onToggleExpanded),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(240)) { -it / 5 },
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(200)) { -it / 6 },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorTokens.purple.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = standardDistanceLabel,
                        style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = ColorTokens.purple,
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(216.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        SectionTitle(
                            text = "测量项",
                            hidden = allComputedHidden,
                            enabled = hasComputedItems,
                            onToggle = {
                                if (allComputedHidden) {
                                    onShowComputed()
                                } else {
                                    onHideComputed()
                                }
                            },
                        )
                    }
                    items(computedMeasurements, key = { it.key }) { item ->
                        val hidden = hiddenKeys.contains(item.key)
                        MeasurementRow(
                            title = item.type,
                            value = item.value,
                            hidden = hidden,
                            valueColor = AnalysisMeasurementPalette.valueColorFor(item),
                            onToggle = { onToggleItemVisibility(item.key) },
                            onDelete = { onDeleteItem(item.key) },
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(colors.borderSubtle, RoundedCornerShape(6.dp))
                                .height(1.dp),
                        )
                    }
                    item {
                        SectionTitle(
                            text = "关键点",
                            hidden = allDetectedHidden,
                            enabled = hasDetectedItems,
                            onToggle = {
                                if (allDetectedHidden) {
                                    onShowDetected()
                                } else {
                                    onHideDetected()
                                }
                            },
                        )
                    }
                    items(detectedPoseFields, key = { it.key }) { item ->
                        val hidden = hiddenKeys.contains(item.key)
                        MeasurementRow(
                            title = item.pointLabel ?: item.type,
                            value = "",
                            hidden = hidden,
                            valueColor = AnalysisMeasurementPalette.valueColorFor(item),
                            onToggle = { onToggleItemVisibility(item.key) },
                            onDelete = { onDeleteItem(item.key) },
                            showValue = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    hidden: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        AppIcon(
            glyph = if (hidden) IconToken.EYE_OFF else IconToken.EYE,
            tint = if (enabled) colors.textSecondary else colors.textTertiary,
            modifier = Modifier.clickable(enabled = enabled, onClick = onToggle),
        )
    }
}

@Composable
private fun MeasurementRow(
    title: String,
    value: String,
    hidden: Boolean,
    valueColor: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    showValue: Boolean = true,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            glyph = if (hidden) IconToken.EYE_OFF else IconToken.EYE,
            tint = if (hidden) colors.textTertiary else colors.textSecondary,
            modifier = Modifier.clickable(onClick = onToggle),
        )
        Text(
            text = title,
            style = SpineTheme.typography.subhead,
            color = if (hidden) colors.textTertiary else colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
            maxLines = 1,
        )
        if (showValue) {
            Text(
                text = value,
                style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = if (hidden) colors.textTertiary else valueColor,
                maxLines = 1,
            )
        }
        AppIcon(
            glyph = IconToken.DELETE,
            tint = if (hidden) colors.textTertiary else colors.textSecondary,
            modifier = Modifier
                .padding(start = 6.dp)
                .clickable(onClick = onDelete),
        )
    }
}

private object ColorTokens {
    val purple = androidx.compose.ui.graphics.Color(0xFF8A57D9)
}




