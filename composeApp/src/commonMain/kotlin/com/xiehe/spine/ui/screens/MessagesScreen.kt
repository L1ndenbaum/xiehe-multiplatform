package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.NotificationMessage
import com.xiehe.spine.data.NotificationRepository
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.AppIcon
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.errorMessage != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.errorMessage ?: "",
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
                MessageCard(item)
            }
        }

        if (state.loading) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

@Composable
private fun MessageCard(item: NotificationMessage) {
    val colors = SpineTheme.colors
    val style = typeStyle(item)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(style.background, RoundedCornerShape(SpineTheme.radius.md)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(glyph = style.icon, tint = style.iconTint, modifier = Modifier.size(16.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.title,
                        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = messageTimeLabel(item),
                        style = SpineTheme.typography.caption,
                        color = colors.textTertiary,
                    )
                }
                item.messageType?.takeIf { it.isNotBlank() }?.let { type ->
                    Text(
                        text = type,
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = style.iconTint,
                        modifier = Modifier
                            .background(style.background, RoundedCornerShape(SpineTheme.radius.full))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Text(text = item.content, style = SpineTheme.typography.subhead, color = colors.textSecondary)
            }
        }
    }
}

private data class MessageTypeStyle(
    val icon: IconToken,
    val iconTint: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
)

@Composable
private fun typeStyle(message: NotificationMessage): MessageTypeStyle {
    val colors = SpineTheme.colors
    val type = message.messageType.orEmpty()
    return when {
        type.contains("系统") -> MessageTypeStyle(
            icon = IconToken.BELL,
            iconTint = colors.primary,
            background = colors.primaryMuted,
        )

        type.contains("审核") -> MessageTypeStyle(
            icon = IconToken.CHECK,
            iconTint = colors.warning,
            background = colors.warning.copy(alpha = 0.16f),
        )

        else -> MessageTypeStyle(
            icon = IconToken.MESSAGE,
            iconTint = colors.textSecondary,
            background = colors.surfaceMuted,
        )
    }
}

private fun messageTimeLabel(message: NotificationMessage): String {
    val source = message.createdAt ?: return ""
    return source.replace("T", " ").take(16)
}
