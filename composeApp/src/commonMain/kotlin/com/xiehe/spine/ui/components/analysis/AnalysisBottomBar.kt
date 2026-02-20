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
    AI_MEASURE,
    REPORT,
    TOOLKIT,
    SETTINGS,
}

@Composable
fun AnalysisBottomBar(
    modifier: Modifier = Modifier,
    onAction: (AnalysisBottomAction) -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(Color(0xFF122438))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnalysisBottomItem(
            label = "AI检测",
            icon = IconToken.AI_DETECT,
            modifier = Modifier.weight(1f),
            onClick = { onAction(AnalysisBottomAction.AI_DETECT) },
        )
        AnalysisBottomItem(
            label = "AI测量",
            icon = IconToken.AI_MEASURE,
            modifier = Modifier.weight(1f),
            onClick = { onAction(AnalysisBottomAction.AI_MEASURE) },
        )
        AnalysisBottomItem(
            label = "报告",
            icon = IconToken.REPORT,
            modifier = Modifier.weight(1f),
            onClick = { onAction(AnalysisBottomAction.REPORT) },
        )
        AnalysisBottomItem(
            label = "工具",
            icon = IconToken.MEASURE_TOOLKIT,
            modifier = Modifier.weight(1f),
            onClick = { onAction(AnalysisBottomAction.TOOLKIT) },
        )
        AnalysisBottomItem(
            label = "设置",
            icon = IconToken.SETTINGS,
            modifier = Modifier.weight(1f),
            onClick = { onAction(AnalysisBottomAction.SETTINGS) },
        )
    }
}

@Composable
private fun AnalysisBottomItem(
    label: String,
    icon: IconToken,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppIcon(
            glyph = icon,
            tint = colors.onPrimary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onPrimary,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
