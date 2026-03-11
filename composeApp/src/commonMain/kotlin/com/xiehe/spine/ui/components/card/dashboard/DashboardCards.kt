package com.xiehe.spine.ui.components.card.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.ui.components.badge.shared.IconBadge
import com.xiehe.spine.ui.components.card.image.inferExamType
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun DashboardSectionTitle(title: String) {
    Text(
        text = title,
        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF334155),
    )
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: IconToken,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(colors))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = icon, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Text(
            text = value,
            style = SpineTheme.typography.display.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
        )
        Text(text = title, style = SpineTheme.typography.caption, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
fun QuickActionItem(
    label: String,
    icon: IconToken,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconBadge(
            glyph = icon,
            colors = colors,
            size = 56.dp,
        )
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF475569),
        )
    }
}

@Composable
fun PendingTaskCard(
    items: List<ImageFileSummary>,
    onOpenAnalysis: (Int, Int?, String) -> Unit,
    onOpenImagesTab: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))),
                )
                Text(
                    text = "待处理任务",
                    style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFF7ED))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${items.size} 项待审",
                    style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF97316),
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                text = "暂无待处理影像",
                style = SpineTheme.typography.subhead,
                color = SpineTheme.colors.textSecondary,
            )
        } else {
            items.forEachIndexed { index, item ->
                PendingTaskRow(item = item, onOpenAnalysis = onOpenAnalysis)
                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF8FAFC)),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFC4B5FD), RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(onClick = onOpenImagesTab)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "查看全部影像",
                style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF7C3AED),
            )
            AppIcon(
                glyph = IconToken.ARROW_RIGHT,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.padding(start = 6.dp).size(16.dp),
            )
        }
    }
}

@Composable
private fun PendingTaskRow(
    item: ImageFileSummary,
    onOpenAnalysis: (Int, Int?, String) -> Unit,
) {
    val patientName = item.patientName?.trim().takeUnless { it.isNullOrBlank() }
        ?: item.patientId?.let { "患者 $it" }
        ?: "未绑定患者"
    val examType = inferExamType(item)
    val examStyle = pendingTaskExamStyle(examType)
    val priority = pendingTaskPriority(item)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(priority.dotColor),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F3FF)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = IconToken.USER_ROUND, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = patientName,
                    modifier = Modifier.weight(1f, fill = false),
                    style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(examStyle.background)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = examType,
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = examStyle.textColor,
                        maxLines = 1,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = buildTaskMeta(item),
                    modifier = Modifier.weight(1f),
                    style = SpineTheme.typography.caption,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                )
                Text(
                    text = priority.label,
                    style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                    color = priority.textColor,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))))
                .clickable { onOpenAnalysis(item.id, item.patientId, examType) },
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = IconToken.ARROW_RIGHT, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

private data class PendingTaskExamStyle(
    val background: Color,
    val textColor: Color,
)

private data class PendingTaskPriority(
    val dotColor: Color,
    val textColor: Color,
    val label: String,
)

private fun pendingTaskExamStyle(examType: String): PendingTaskExamStyle {
    return when {
        examType.contains("体态") -> PendingTaskExamStyle(Color(0xFFFFF1F2), Color(0xFFE11D48))
        examType.contains("侧") -> PendingTaskExamStyle(Color(0xFFFFF7ED), Color(0xFFEA580C))
        examType.contains("曲") -> PendingTaskExamStyle(Color(0xFFECFDF5), Color(0xFF059669))
        examType.contains("CT") -> PendingTaskExamStyle(Color(0xFFF0F9FF), Color(0xFF0284C7))
        examType.contains("MRI") -> PendingTaskExamStyle(Color(0xFFF5F3FF), Color(0xFF7C3AED))
        else -> PendingTaskExamStyle(Color(0xFFF0FDFA), Color(0xFF0D9488))
    }
}

private fun pendingTaskPriority(item: ImageFileSummary): PendingTaskPriority {
    return when (item.status?.uppercase()) {
        "PROCESSING" -> PendingTaskPriority(
            dotColor = Color(0xFFF59E0B),
            textColor = Color(0xFFFB923C),
            label = "普通",
        )

        "UPLOADED" -> PendingTaskPriority(
            dotColor = Color(0xFFEF4444),
            textColor = Color(0xFFF87171),
            label = "紧急",
        )

        else -> PendingTaskPriority(
            dotColor = Color(0xFFCBD5E1),
            textColor = Color(0xFF94A3B8),
            label = "常规",
        )
    }
}

private fun buildTaskMeta(item: ImageFileSummary): String {
    val patientCode = item.patientId?.let { "P$it" } ?: "未分配编号"
    val timeLabel = extractTaskTime(item.createdAt)
    return if (timeLabel.isBlank()) {
        patientCode
    } else {
        "$patientCode · $timeLabel"
    }
}

private fun extractTaskTime(createdAt: String?): String {
    val normalized = createdAt?.replace("T", " ")?.trim().orEmpty()
    if (normalized.isBlank()) {
        return ""
    }
    return Regex("""(\d{2}:\d{2})""").find(normalized)?.value ?: normalized.take(16)
}
