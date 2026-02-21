package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.NotificationMessage
import com.xiehe.spine.data.NotificationRepository
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.PeriodicTaskTrigger
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.MessagesViewModel

@Composable
fun MessagesScreen(
    vm: MessagesViewModel,
    session: UserSession,
    repository: NotificationRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()

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

    Column(
        modifier = Modifier
            .background(SpineTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.errorMessage?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = it,
                    style = SpineTheme.typography.subhead,
                    color = SpineTheme.colors.error,
                )
            }
        }

        if (!state.loading && state.items.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "暂无消息",
                    style = SpineTheme.typography.subhead,
                    color = SpineTheme.colors.textSecondary,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.items, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = item.title, style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = messageTimeLabel(item),
                            style = SpineTheme.typography.caption,
                            color = SpineTheme.colors.textTertiary,
                        )
                    }
                    Text(text = item.content, style = SpineTheme.typography.subhead, color = SpineTheme.colors.textSecondary)
                }
            }
        }
    }

    if (state.loading) {
        LoadingOverlay(message = "...正在加载中")
    }
}

private fun messageTimeLabel(message: NotificationMessage): String {
    val source = message.createdAt ?: return ""
    return source.replace("T", " ").take(16)
}
