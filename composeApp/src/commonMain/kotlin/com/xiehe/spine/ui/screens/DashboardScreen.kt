package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.data.NotificationMessage
import com.xiehe.spine.data.NotificationRepository
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.badge.shared.IconBadge
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.inferExamType
import com.xiehe.spine.ui.components.message.messageTimeLabel
import com.xiehe.spine.ui.components.message.messageTypeStyle
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    session: UserSession,
    dashboardRepository: DashboardRepository,
    imageRepository: ImageFileRepository,
    notificationRepository: NotificationRepository,
    authRepository: AuthRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onOpenAnalysis: (Int, Int?, String) -> Unit = { _, _, _ -> },
    onOpenPatientForm: () -> Unit = {},
    onOpenImageUpload: () -> Unit = {},
    onOpenImagesTab: () -> Unit = {},
    onOpenMessages: () -> Unit = {},
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(session.accessToken) {
        vm.load(
            session = session,
            dashboardRepository = dashboardRepository,
            imageRepository = imageRepository,
            notificationRepository = notificationRepository,
            authRepository = authRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (state.errorMessage != null) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut(),
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.errorMessage ?: "",
                                style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error),
                            )
                        }
                    }
                }
            }

            item {
                DashboardSectionTitle(title = "数据概览")
            }

            val overview = state.data
            if (overview == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (state.loading) "加载工作台数据中..." else "暂无数据",
                            color = SpineTheme.colors.textSecondary,
                        )
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            title = "总患者数",
                            value = overview.totalPatients.toString(),
                            icon = IconToken.HEART_PULSE,
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
                            modifier = Modifier.weight(1f),
                        )
                        DashboardStatCard(
                            title = "总影像数",
                            value = overview.totalImages.toString(),
                            icon = IconToken.SCAN_SEARCH,
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                DashboardSectionTitle(title = "快捷入口")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionItem(
                        label = "新增患者",
                        icon = IconToken.USER_PLUS,
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF9333EA)),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenPatientForm,
                    )
                    QuickActionItem(
                        label = "上传影像",
                        icon = IconToken.UPLOAD,
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB)),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImageUpload,
                    )
                    QuickActionItem(
                        label = "影像中心",
                        icon = IconToken.SCAN_SEARCH,
                        colors = listOf(Color(0xFF34D399), Color(0xFF0D9488)),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImagesTab,
                    )
                    QuickActionItem(
                        label = "消息通知",
                        icon = IconToken.BELL_RING,
                        colors = listOf(Color(0xFFFB923C), Color(0xFFF43F5E)),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMessages,
                    )
                }
            }

            item {
                PendingTaskCard(
                    items = state.pendingItems.take(5),
                    onOpenAnalysis = onOpenAnalysis,
                    onOpenImagesTab = onOpenImagesTab,
                )
            }

            item {
                ActivityCard(items = state.recentMessages)
            }
        }

        if (state.loading && state.data == null) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

@Composable
private fun DashboardSectionTitle(title: String) {
    Text(
        text = title,
        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF334155),
    )
}

@Composable
private fun DashboardStatCard(
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
private fun QuickActionItem(
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
private fun PendingTaskCard(
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
@Composable
private fun ActivityCard(items: List<NotificationMessage>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF2563EB)))),
            )
            Text(
                text = "近期动态",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A),
            )
        }

        if (items.isEmpty()) {
            Text(
                text = "暂无动态",
                style = SpineTheme.typography.subhead,
                color = SpineTheme.colors.textSecondary,
            )
        } else {
            items.forEachIndexed { index, activity ->
                ActivityRow(activity = activity)
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
    }
}

@Composable
private fun ActivityRow(activity: NotificationMessage) {
    val style = messageTypeStyle(activity.messageType, SpineTheme.colors)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(style.background),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = style.icon, tint = style.iconTint, modifier = Modifier.size(16.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = activity.title,
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF334155),
            )
            Text(
                text = activity.content,
                style = SpineTheme.typography.caption,
                color = SpineTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = messageTimeLabel(activity.createdAt),
            style = SpineTheme.typography.caption,
            color = SpineTheme.colors.textTertiary,
        )
    }
}