package com.xiehe.spine.ui.components.analysis.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.components.text.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun AnalysisSettingsPanel(
    zoomPercent: Int,
    contrast: Int,
    brightness: Int,
    onZoomChange: (Int) -> Unit,
    onContrastChange: (Int) -> Unit,
    onBrightnessChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(colors.backgroundElevated.copy(alpha = 0.98f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colors.primaryMuted, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    glyph = IconToken.MEASURE_ZOOM,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = "影像缩放",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
        }

        AnalyzerStepper(
            label = "缩放",
            value = "$zoomPercent%",
            onMinus = { onZoomChange(-10) },
            onPlus = { onZoomChange(10) },
        )
        AnalyzerStepper(
            label = "对比度",
            value = contrast.toString(),
            onMinus = { onContrastChange(-5) },
            onPlus = { onContrastChange(5) },
        )
        AnalyzerStepper(
            label = "亮度",
            value = brightness.toString(),
            onMinus = { onBrightnessChange(-5) },
            onPlus = { onBrightnessChange(5) },
        )
    }
}

@Composable
private fun AnalyzerStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SpineTheme.typography.body,
            color = colors.textPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(icon = IconToken.MINUS, onClick = onMinus)
            Text(
                text = value,
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
            )
            StepperButton(icon = IconToken.ADD, onClick = onPlus)
        }
    }
}

@Composable
private fun StepperButton(
    icon: IconToken,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    Box(
        modifier = Modifier
            .background(colors.surfaceMuted, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(glyph = icon, tint = colors.textPrimary)
    }
}
