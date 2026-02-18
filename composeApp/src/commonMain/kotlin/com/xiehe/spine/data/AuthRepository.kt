package com.xiehe.spine.data

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.SessionStore
import com.xiehe.spine.core.store.UserSession

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionStore: SessionStore,
) {
    private val refreshLeewaySeconds = 180L

    suspend fun healthCheck(): AppResult<HealthData> {
        return apiClient.get(path = "/health/")
    }

    fun restoreSession(): UserSession? {
        val loaded = sessionStore.load() ?: return null
        if (loaded.accessTokenExpiresAtEpochSeconds != null) {
            return loaded
        }
        val patched = loaded.copy(accessTokenExpiresAtEpochSeconds = currentEpochSeconds() + 300L)
        sessionStore.save(patched)
        return patched
    }

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
                    accessTokenExpiresAtEpochSeconds = currentEpochSeconds() + payload.expiresIn.toLong(),
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
                    accessTokenExpiresAtEpochSeconds = currentEpochSeconds() + payload.expiresIn.toLong(),
                )
                sessionStore.save(next)
                AppResult.Success(next)
            }

            is AppResult.Failure -> {
                if (result.isUnauthorized) {
                    sessionStore.clear()
                }
                result
            }
        }
    }

    suspend fun ensureFreshSession(session: UserSession): AppResult<UserSession> {
        val expiresAt = session.accessTokenExpiresAtEpochSeconds ?: return AppResult.Success(session)
        val remainingSeconds = expiresAt - currentEpochSeconds()
        return if (remainingSeconds > refreshLeewaySeconds) {
            AppResult.Success(session)
        } else {
            refreshToken(session)
        }
    }

    fun logout() {
        sessionStore.clear()
    }
}
