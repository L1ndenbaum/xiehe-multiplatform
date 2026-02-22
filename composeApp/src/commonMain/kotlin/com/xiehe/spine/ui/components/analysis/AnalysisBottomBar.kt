package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

enum class AnalysisBottomAction {
    AI_DETECT,
    REPORT,
    TOOLKIT,
    SETTINGS,
}

@Composable
fun AnalysisBottomBar(
    modifier: Modifier = Modifier,
    onAction: (AnalysisBottomAction) -> Unit,
) {
    val barBackground = Color(0xFF1E3552)
    val itemBackground = Color(0xFF324D70)
    val contentColor = Color(0xFFF2F7FF)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(barBackground)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnalysisBottomItem(
            label = "AI检测",
            icon = IconToken.AI_DETECT,
            modifier = Modifier.weight(1f),
            containerColor = itemBackground,
            contentColor = contentColor,
            onClick = { onAction(AnalysisBottomAction.AI_DETECT) },
        )
        AnalysisBottomItem(
            label = "报告",
            icon = IconToken.REPORT,
            modifier = Modifier.weight(1f),
            containerColor = itemBackground,
            contentColor = contentColor,
            onClick = { onAction(AnalysisBottomAction.REPORT) },
        )
        AnalysisBottomItem(
            label = "工具",
            icon = IconToken.MEASURE_TOOLKIT,
            modifier = Modifier.weight(1f),
            containerColor = itemBackground,
            contentColor = contentColor,
            onClick = { onAction(AnalysisBottomAction.TOOLKIT) },
        )
        AnalysisBottomItem(
            label = "设置",
            icon = IconToken.SETTINGS,
            modifier = Modifier.weight(1f),
            containerColor = itemBackground,
            contentColor = contentColor,
            onClick = { onAction(AnalysisBottomAction.SETTINGS) },
        )
    }
}

@Composable
private fun AnalysisBottomItem(
    label: String,
    icon: IconToken,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppIcon(
            glyph = icon,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
