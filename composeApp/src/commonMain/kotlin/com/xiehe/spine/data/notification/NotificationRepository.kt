package com.xiehe.spine.data.notification

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ApiClient
import com.xiehe.spine.data.auth.AuthRepository

class NotificationRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadMessages(
        session: UserSession,
        page: Int = 1,
        pageSize: Int = 20,
        messageType: String? = null,
        isRead: Boolean? = null,
    ): AppResult<Pair<UserSession, NotificationMessagePageData>> {
        val path = buildString {
            append("/notifications/messages?skip=")
            append(((page - 1).coerceAtLeast(0)) * pageSize)
            append("&limit=")
            append(pageSize)
            messageType?.trim()?.takeIf { it.isNotBlank() }?.let {
                append("&message_type=")
                append(it)
            }
            isRead?.let {
                append("&is_read=")
                append(it)
            }
        }
        return withRefresh(session) { activeSession ->
            apiClient.get(path = path, accessToken = activeSession.accessToken)
        }
    }

    suspend fun loadStats(
        session: UserSession,
    ): AppResult<Pair<UserSession, NotificationStats>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/notifications/messages/stats", accessToken = activeSession.accessToken)
        }
    }

    suspend fun getSettings(
        session: UserSession,
    ): AppResult<Pair<UserSession, NotificationSettings>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/notifications/settings", accessToken = activeSession.accessToken)
        }
    }

    suspend fun updateSettings(
        session: UserSession,
        request: NotificationSettingsUpdateRequest,
    ): AppResult<Pair<UserSession, NotificationSettings>> {
        return withRefresh(session) { activeSession ->
            apiClient.put(path = "/notifications/settings", body = request, accessToken = activeSession.accessToken)
        }
    }

    suspend fun markMessageRead(
        session: UserSession,
        messageId: Int,
    ): AppResult<Pair<UserSession, String>> {
        return withRefresh(session) { activeSession ->
            apiClient.put<MarkMessageReadData, Map<String, String>>(
                path = "/notifications/messages/$messageId/read",
                body = emptyMap(),
                accessToken = activeSession.accessToken,
            ).let { result ->
                when (result) {
                    is AppResult.Success -> AppResult.Success("消息已标记为已读")
                    is AppResult.Failure -> result
                }
            }
        }
    }

    suspend fun deleteMessage(
        session: UserSession,
        messageId: Int,
    ): AppResult<Pair<UserSession, String>> {
        return withRefresh(session) { activeSession ->
            apiClient.deleteForMessage(path = "/notifications/messages/$messageId", accessToken = activeSession.accessToken)
        }
    }

    suspend fun sendMessage(
        session: UserSession,
        request: NotificationMessageCreateRequest,
    ): AppResult<Pair<UserSession, String>> {
        return withRefresh(session) { activeSession ->
            apiClient.postForMessage(path = "/notifications/messages", body = request, accessToken = activeSession.accessToken)
        }
    }

    private suspend inline fun <reified T> withRefresh(
        session: UserSession,
        crossinline action: suspend (UserSession) -> AppResult<T>,
    ): AppResult<Pair<UserSession, T>> {
        return when (val first = action(session)) {
            is AppResult.Success -> AppResult.Success(session to first.data)
            is AppResult.Failure -> {
                if (!first.isUnauthorized) {
                    first
                } else {
                    when (val refreshed = authRepository.refreshToken(session)) {
                        is AppResult.Success -> {
                            when (val second = action(refreshed.data)) {
                                is AppResult.Success -> AppResult.Success(refreshed.data to second.data)
                                is AppResult.Failure -> second
                            }
                        }

                        is AppResult.Failure -> refreshed
                    }
                }
            }
        }
    }
}
