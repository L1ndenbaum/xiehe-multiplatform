package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.SessionStore
import com.xiehe.spine.core.store.UserSession

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionStore: SessionStore,
) {
    suspend fun healthCheck(): AppResult<HealthData> {
        return apiClient.get(path = "/health/")
    }

    fun restoreSession(): UserSession? = sessionStore.load()

    suspend fun login(username: String, password: String): AppResult<UserSession> {
        return when (
            val result = apiClient.post<LoginData, LoginRequest>(
                path = "/auth/login",
                body = LoginRequest(username = username, password = password, rememberMe = false),
            )
        ) {
            is AppResult.Success -> {
                val payload = result.data
                val session = UserSession(
                    accessToken = payload.accessToken,
                    refreshToken = payload.refreshToken,
                    userId = payload.user.id,
                    username = payload.user.username,
                    email = payload.user.email,
                    fullName = payload.user.fullName,
                )
                sessionStore.save(session)
                AppResult.Success(session)
            }

            is AppResult.Failure -> result
        }
    }

    suspend fun refreshToken(session: UserSession): AppResult<UserSession> {
        return when (
            val result = apiClient.post<LoginData, RefreshRequest>(
                path = "/auth/refresh",
                body = RefreshRequest(refreshToken = session.refreshToken),
            )
        ) {
            is AppResult.Success -> {
                val payload = result.data
                val next = session.copy(
                    accessToken = payload.accessToken,
                    refreshToken = payload.refreshToken,
                )
                sessionStore.save(next)
                AppResult.Success(next)
            }

            is AppResult.Failure -> {
                sessionStore.clear()
                result
            }
        }
    }

    fun logout() {
        sessionStore.clear()
    }
}
