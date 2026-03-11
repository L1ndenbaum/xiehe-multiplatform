package com.xiehe.spine.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.auth.AuthRepository
import com.xiehe.spine.data.dashboard.DashboardRepository
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.notification.NotificationRepository
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.card.dashboard.ActivityCard
import com.xiehe.spine.ui.components.card.dashboard.DashboardSectionTitle
import com.xiehe.spine.ui.components.card.dashboard.DashboardStatCard
import com.xiehe.spine.ui.components.card.dashboard.PendingTaskCard
import com.xiehe.spine.ui.components.card.dashboard.QuickActionItem
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.dashboard.DashboardViewModel

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



