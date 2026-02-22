package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.AppIcon
import com.xiehe.spine.ui.components.ImageTaskAction
import com.xiehe.spine.ui.components.ImageTaskActionStyle
import com.xiehe.spine.ui.components.ImageTaskCard
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.ProgressRing
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.inferExamType
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    session: UserSession,
    dashboardRepository: DashboardRepository,
    imageRepository: ImageFileRepository,
    authRepository: AuthRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onOpenAnalysis: (Int, Int?, String) -> Unit = { _, _, _ -> },
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing

    LaunchedEffect(session.accessToken) {
        vm.load(
            session = session,
            dashboardRepository = dashboardRepository,
            imageRepository = imageRepository,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.base),
        ) {
            item {
                AnimatedVisibility(
                    visible = state.errorMessage != null,
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

            val overview = state.data
            if (overview == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = if (state.loading) "加载工作台数据中..." else "暂无数据")
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                        StatCard("累计患者", overview.totalPatients.toString(), IconToken.USERS, modifier = Modifier.weight(1f))
                        StatCard("待处理影像", overview.pendingImages.toString(), IconToken.HOURGLASS, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                        StatCard("已完成影像", overview.processedImages.toString(), IconToken.CHECK, modifier = Modifier.weight(1f))
                        StatCard("累计影像", overview.totalImages.toString(), IconToken.IMAGE, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("完成率 ${overview.completionRate}%", style = SpineTheme.typography.title)
                                Text("平均处理时长 ${overview.averageProcessingTime} 小时")
                                Text("系统提醒 ${overview.systemAlerts}")
                            }
                            ProgressRing(progress = (overview.completionRate / 100f).toFloat())
                        }
                    }
                }
            }

            item {
                Text(
                    text = "待处理任务",
                    style = SpineTheme.typography.title,
                    color = SpineTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
            }

            if (state.pendingItems.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (state.loading) "加载中..." else "暂无待处理影像",
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.textSecondary,
                        )
                    }
                }
            } else {
                items(state.pendingItems, key = { it.id }) { file ->
                    ImageTaskCard(
                        item = file,
                        session = session,
                        repository = imageRepository,
                        onSessionUpdated = onSessionUpdated,
                        compactActionText = true,
                        singleActionBottomRight = true,
                        actions = listOf(
                            ImageTaskAction(
                                text = "立即处理",
                                glyph = IconToken.EYE,
                                style = ImageTaskActionStyle.PRIMARY,
                                onClick = {
                                    onOpenAnalysis(
                                        file.id,
                                        file.patientId,
                                        inferExamType(file),
                                    )
                                },
                            ),
                        ),
                    )
                }
            }
        }

        if (state.loading && state.data == null) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    glyph: IconToken,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = SpineTheme.typography.subhead)
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .background(
                        color = SpineTheme.colors.primaryMuted,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    glyph = glyph,
                    tint = SpineTheme.colors.primary,
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp),
                )
            }
        }
        Text(text = value, style = SpineTheme.typography.display.copy(fontSize = 38.sp))
    }
}
