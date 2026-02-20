package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class ImageFileRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadImageFiles(session: UserSession): AppResult<Pair<UserSession, ImageFilePageData>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/image-files", accessToken = activeSession.accessToken)
        }
    }

    suspend fun downloadImageBytes(
        session: UserSession,
        fileId: Int,
    ): AppResult<Pair<UserSession, ByteArray>> {
        return withRefresh(session) { activeSession ->
            val requestUrl = "${apiClient.baseUrl}/image-files/$fileId/download"
            try {
                val bytes = apiClient.httpClient.get(requestUrl) {
                    header(HttpHeaders.Authorization, "Bearer ${activeSession.accessToken}")
                }.body<ByteArray>()
                AppResult.Success(bytes)
            } catch (e: ClientRequestException) {
                val status = e.response.status
                AppResult.Failure(
                    message = "下载影像失败",
                    code = status.value,
                    isUnauthorized = status == HttpStatusCode.Unauthorized,
                    debugDetails = "[GET] $requestUrl status=${status.value}",
                )
            } catch (e: Exception) {
                AppResult.Failure(
                    message = apiClient.classifyNetworkError(e.message),
                    debugDetails = "[GET] $requestUrl message=${e.message ?: "N/A"}",
                )
            }
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
