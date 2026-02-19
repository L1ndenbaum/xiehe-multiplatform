package com.xiehe.spine.data

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.SessionStore
import com.xiehe.spine.core.store.UserSession
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        val patched = loaded.copy(
            accessTokenExpiresAtEpochSeconds = resolveAccessTokenExpiry(
                accessToken = loaded.accessToken,
                expiresIn = null,
            ) ?: (currentEpochSeconds() + 300L),
        )
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
                    avatarUrl = payload.user.avatarUrl,
                    avatar = payload.user.avatar,
                    accessTokenExpiresAtEpochSeconds = resolveAccessTokenExpiry(
                        accessToken = payload.accessToken,
                        expiresIn = payload.expiresIn,
                    ),
                )
                sessionStore.save(session)
                AppResult.Success(session)
            }

            is AppResult.Failure -> result
        }
    }

    suspend fun refreshToken(session: UserSession): AppResult<UserSession> {
        return when (
            val result = apiClient.post<RefreshData, RefreshRequest>(
                path = "/auth/refresh",
                body = RefreshRequest(refreshToken = session.refreshToken),
            )
        ) {
            is AppResult.Success -> {
                val payload = result.data.tokens
                val next = session.copy(
                    accessToken = payload.accessToken,
                    refreshToken = payload.refreshToken,
                    accessTokenExpiresAtEpochSeconds = resolveAccessTokenExpiry(
                        accessToken = payload.accessToken,
                        expiresIn = payload.expiresIn,
                    ),
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

    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phone: String?,
    ): AppResult<UserDto> {
        return when (
            val result = apiClient.post<RegisterData, RegisterRequest>(
                path = "/auth/register",
                body = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    fullName = fullName,
                    phone = phone,
                ),
            )
        ) {
            is AppResult.Success -> AppResult.Success(result.data.user)
            is AppResult.Failure -> result
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

    @OptIn(ExperimentalEncodingApi::class)
    private fun resolveAccessTokenExpiry(
        accessToken: String,
        expiresIn: Int?,
    ): Long? {
        if (expiresIn != null && expiresIn > 0) {
            return currentEpochSeconds() + expiresIn.toLong()
        }
        val payload = accessToken.split('.').getOrNull(1) ?: return null
        val padded = payload
            .replace('-', '+')
            .replace('_', '/')
            .let {
                val padLen = (4 - (it.length % 4)) % 4
                it + "=".repeat(padLen)
            }
        val decodedJson = runCatching { Base64.decode(padded).decodeToString() }.getOrNull() ?: return null
        return runCatching {
            Json.parseToJsonElement(decodedJson)
                .jsonObject["exp"]
                ?.jsonPrimitive
                ?.content
                ?.toLong()
        }.getOrNull()
    }
}
