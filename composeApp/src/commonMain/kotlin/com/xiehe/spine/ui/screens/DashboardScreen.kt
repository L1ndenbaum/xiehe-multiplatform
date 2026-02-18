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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineGlyphIcon
import com.xiehe.spine.ui.components.SpineMiniBarChart
import com.xiehe.spine.ui.components.SpineProgressRing
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    session: UserSession,
    repository: DashboardRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing

    LaunchedEffect(session.accessToken) {
        vm.load(session, repository, onSessionUpdated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(
                    text = state.errorMessage ?: "",
                    style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error),
                )
            }
        }

        val overview = state.data
        if (overview == null) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = if (state.loading) "加载工作台数据中..." else "暂无数据")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                StatCard("累计患者", overview.totalPatients.toString(), SpineGlyph.USERS, modifier = Modifier.weight(1f))
                StatCard("待处理影像", overview.pendingImages.toString(), SpineGlyph.HOURGLASS, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                StatCard("已完成影像", overview.processedImages.toString(), SpineGlyph.CHECK, modifier = Modifier.weight(1f))
                StatCard("累计影像", overview.totalImages.toString(), SpineGlyph.IMAGE, modifier = Modifier.weight(1f))
            }
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SpineText("完成率 ${overview.completionRate}%", style = SpineTheme.typography.title)
                        SpineText("平均处理时长 ${overview.averageProcessingTime} 小时")
                        SpineText("系统提醒 ${overview.systemAlerts}")
                    }
                    SpineProgressRing(progress = (overview.completionRate / 100f).toFloat())
                }
                SpineMiniBarChart(
                    values = listOf(
                        overview.newPatientsToday.toFloat(),
                        (overview.newPatientsWeek / 7f),
                        overview.imagesToday.toFloat(),
                        (overview.imagesWeek / 7f),
                    ),
                    labels = listOf("今患", "周均患", "今影", "周均影"),
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    glyph: SpineGlyph,
    modifier: Modifier = Modifier,
) {
    SpineCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpineText(text = title, style = SpineTheme.typography.subhead)
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
                SpineGlyphIcon(
                    glyph = glyph,
                    tint = SpineTheme.colors.primary,
                    modifier = Modifier.width(16.dp).height(16.dp),
                )
            }
        }
        SpineText(text = value, style = SpineTheme.typography.display.copy(fontSize = 38.sp))
    }
}
