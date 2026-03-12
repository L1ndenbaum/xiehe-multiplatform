package com.xiehe.spine.ui.screens.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.notification.NotificationRepository
import com.xiehe.spine.ui.components.card.message.MessageNoticeCard
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.PeriodicTaskTrigger
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.message.MessagesViewModel

@Composable
fun MessagesScreen(
    vm: MessagesViewModel,
    session: UserSession,
    repository: NotificationRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()
    var localNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(session.accessToken) {
        vm.load(
            session = session,
            repository = repository,
            onSessionUpdated = onSessionUpdated,
            silent = false,
        )
    }

    PeriodicTaskTrigger(
        intervalMillis = 60_000L,
        enabled = true,
        runImmediately = false,
        key = session.accessToken,
    ) {
        vm.load(
            session = session,
            repository = repository,
            onSessionUpdated = onSessionUpdated,
            silent = true,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.backgroundElevated),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            localNotice?.let {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = it,
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.warning,
                        )
                    }
                }
            }

            state.errorMessage?.let {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = it,
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.error,
                        )
                    }
                }
            }

            if (!state.loading && state.items.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        SpineTheme.colors.surfaceMuted,
                                        RoundedCornerShape(SpineTheme.radius.full),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(
                                    glyph = IconToken.MESSAGE,
                                    tint = SpineTheme.colors.textTertiary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Text("暂无消息", color = SpineTheme.colors.textSecondary)
                        }
                    }
                }
            }

            items(state.items, key = { it.id }) { item ->
                MessageNoticeCard(
                    item = item,
                    onDelete = {
                        localNotice = "当前版本尚未接入删除消息接口"
                    },
                )
            }
        }

        if (state.loading) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}
