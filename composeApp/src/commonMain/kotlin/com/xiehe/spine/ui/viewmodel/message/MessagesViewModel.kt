package com.xiehe.spine.ui.viewmodel.message
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.notification.NotificationMessage
import com.xiehe.spine.data.notification.NotificationSettings
import com.xiehe.spine.data.notification.NotificationStats
import com.xiehe.spine.data.notification.NotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagesUiState(
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val items: List<NotificationMessage> = emptyList(),
    val stats: NotificationStats? = null,
    val settings: NotificationSettings? = null,
    val noticeMessage: String? = null,
    val errorMessage: String? = null,
)

class MessagesViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(MessagesUiState())
    val state: StateFlow<MessagesUiState> = _state.asStateFlow()

    fun load(
        session: UserSession,
        repository: NotificationRepository,
        onSessionUpdated: (UserSession) -> Unit,
        silent: Boolean = false,
    ) {
        scope.launch {
            if (!silent) {
                _state.update { it.copy(loading = true, errorMessage = null, noticeMessage = null) }
            }
            val messagesDeferred = async { repository.loadMessages(session = session, page = 1, pageSize = 50) }
            val statsDeferred = async { repository.loadStats(session) }
            val settingsDeferred = async { repository.getSettings(session) }

            val messagesResult = messagesDeferred.await()
            val statsResult = statsDeferred.await()
            val settingsResult = settingsDeferred.await()

            listOf(messagesResult, statsResult, settingsResult)
                .filterIsInstance<AppResult.Success<Pair<UserSession, *>>>()
                .lastOrNull()
                ?.data
                ?.first
                ?.let(onSessionUpdated)

            val messages = (messagesResult as? AppResult.Success)?.data?.second?.items.orEmpty()
            val stats = (statsResult as? AppResult.Success)?.data?.second
            val settings = (settingsResult as? AppResult.Success)?.data?.second
            val failureMessage = listOf(messagesResult, statsResult, settingsResult)
                .filterIsInstance<AppResult.Failure>()
                .firstOrNull()
                ?.message

            _state.update {
                it.copy(
                    loading = false,
                    items = messages,
                    stats = stats ?: it.stats,
                    settings = settings ?: it.settings,
                    errorMessage = failureMessage,
                )
            }
        }
    }

    fun markRead(
        session: UserSession,
        repository: NotificationRepository,
        messageId: Int,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val target = _state.value.items.firstOrNull { it.id == messageId } ?: return
        if (target.isRead) {
            return
        }
        scope.launch {
            _state.update { it.copy(actionLoading = true, errorMessage = null, noticeMessage = null) }
            when (val result = repository.markMessageRead(session, messageId)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update { current ->
                        val updatedItems = current.items.map { item ->
                            if (item.id == messageId) item.copy(isRead = true) else item
                        }
                        current.copy(
                            actionLoading = false,
                            items = updatedItems,
                            stats = current.stats?.copy(unreadMessages = (current.stats.unreadMessages - 1).coerceAtLeast(0)),
                            noticeMessage = result.data.second,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(actionLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun delete(
        session: UserSession,
        repository: NotificationRepository,
        message: NotificationMessage,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(actionLoading = true, errorMessage = null, noticeMessage = null) }
            when (val result = repository.deleteMessage(session, message.id)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update { current ->
                        current.copy(
                            actionLoading = false,
                            items = current.items.filterNot { it.id == message.id },
                            stats = current.stats?.copy(
                                totalMessages = (current.stats.totalMessages - 1).coerceAtLeast(0),
                                unreadMessages = if (message.isRead) {
                                    current.stats.unreadMessages
                                } else {
                                    (current.stats.unreadMessages - 1).coerceAtLeast(0)
                                },
                            ),
                            noticeMessage = result.data.second,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(actionLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
