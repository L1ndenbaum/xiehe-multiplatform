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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.AnalysisToolDefinition

@Composable
fun MeasureToolPanel(
    tools: List<AnalysisToolDefinition>,
    activeToolId: String,
    onSelectTool: (String) -> Unit,
) {
    val colors = SpineTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "测量工具",
            style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
        )

        tools
            .groupBy { it.section.title }
            .forEach { (sectionTitle, sectionTools) ->
                ToolSection(
                    title = sectionTitle,
                    tools = sectionTools,
                    activeToolId = activeToolId,
                    onSelectTool = onSelectTool,
                )
            }
    }
}

@Composable
private fun ToolSection(
    title: String,
    tools: List<AnalysisToolDefinition>,
    activeToolId: String,
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
                    val selected = activeToolId == item.id
                    Row(
                        modifier = Modifier
                            .width(72.dp)
                            .background(
                                if (selected) colors.primary.copy(alpha = 0.18f) else colors.surfaceMuted,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { onSelectTool(item.id) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            glyph = item.icon,
                            tint = if (selected) colors.primary else colors.textPrimary,
                        )
                        Text(
                            text = item.label,
                            style = SpineTheme.typography.caption,
                            color = if (selected) colors.primary else colors.textPrimary,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                        )
                    }
                }
                repeat(4 - rowItems.size) {
                    Row(
                        modifier = Modifier
                            .width(72.dp)
                            .background(Color.Transparent, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                    ) { }
                }
            }
        }
    }
}
