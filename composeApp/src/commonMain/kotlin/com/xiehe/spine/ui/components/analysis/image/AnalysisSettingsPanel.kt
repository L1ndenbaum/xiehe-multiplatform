package com.xiehe.spine.ui.components.analysis.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun AnalysisSettingsPanel(
    zoomPercent: Int,
    contrast: Int,
    brightness: Int,
    standardDistanceInput: String,
    isImageLocked: Boolean,
    onClearAll: () -> Unit,
    onZoomChange: (Int) -> Unit,
    onContrastChange: (Int) -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onStandardDistanceChange: (String) -> Unit,
    onToggleImageLock: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            text = "清空全部测量结果",
            onClick = onClearAll,
            modifier = Modifier.fillMaxWidth(),
            customContainerColor = Color(0xFFD93444),
            customContentColor = Color.White,
            leadingGlyph = IconToken.DELETE,
        )

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

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "标准距离 (mm)",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = SpineTheme.colors.textPrimary,
            )
            TextField(
                value = standardDistanceInput,
                onValueChange = onStandardDistanceChange,
                placeholder = "输入标准距离，默认100",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "锁定图像",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = SpineTheme.colors.textPrimary,
            )
            Button(
                text = if (isImageLocked) "已锁定" else "未锁定",
                onClick = onToggleImageLock,
                customContainerColor = if (isImageLocked) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                customContentColor = if (isImageLocked) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary,
                leadingGlyph = IconToken.LOCK,
            )
        }
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


