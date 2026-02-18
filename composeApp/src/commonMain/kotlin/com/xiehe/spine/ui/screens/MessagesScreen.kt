package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.theme.SpineTheme

private data class MessageItem(
    val title: String,
    val content: String,
    val timeText: String,
)

@Composable
fun MessagesScreen() {
    val messages = listOf(
        MessageItem(
            title = "新影像待审核",
            content = "患者张三提交了新的腰椎影像，请及时处理。",
            timeText = "10:20",
        ),
        MessageItem(
            title = "报告已完成",
            content = "李四的颈椎影像报告已生成，请查看。",
            timeText = "昨天",
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        messages.forEach { item ->
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SpineText(text = item.title, style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold))
                    SpineText(text = item.timeText, style = SpineTheme.typography.caption, color = SpineTheme.colors.textTertiary)
                }
                SpineText(text = item.content, style = SpineTheme.typography.subhead, color = SpineTheme.colors.textSecondary)
            }
        }
    }
}
