package com.xiehe.spine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun MeasureToolPanel(
    onSelectTool: (String) -> Unit,
) {
    val colors = SpineTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "测量工具",
            style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
        )

        ToolSection(
            title = "基础模式",
            tools = listOf(
                ToolItem("移动", IconToken.MEASURE_MOVE),
            ),
            onSelectTool = onSelectTool,
        )

        ToolSection(
            title = "测量标注",
            tools = listOf(
                ToolItem("T1 Tilt", IconToken.MEASURE_T1_TILT),
                ToolItem("Cobb", IconToken.MEASURE_COBB),
                ToolItem("CA", IconToken.MEASURE_CA),
                ToolItem("Pelvic", IconToken.MEASURE_PELVIC),
                ToolItem("TS", IconToken.MEASURE_TS),
                ToolItem("AVT", IconToken.MEASURE_AVT),
                ToolItem("标准距离", IconToken.MEASURE_STANDARD_DISTANCE),
            ),
            onSelectTool = onSelectTool,
        )
    }
}

private data class ToolItem(
    val label: String,
    val icon: IconToken,
)

@Composable
private fun ToolSection(
    title: String,
    tools: List<ToolItem>,
    onSelectTool: (String) -> Unit,
) {
    val colors = SpineTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textSecondary,
        )

        val rows = tools.chunked(4)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .width(72.dp)
                            .background(colors.surfaceMuted, RoundedCornerShape(10.dp))
                            .clickable { onSelectTool(item.label) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            glyph = item.icon,
                            tint = colors.textPrimary,
                        )
                        Text(
                            text = item.label,
                            style = SpineTheme.typography.caption,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
