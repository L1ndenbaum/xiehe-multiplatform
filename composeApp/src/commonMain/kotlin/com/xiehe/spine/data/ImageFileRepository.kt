package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import io.ktor.client.request.delete
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Headers
import kotlinx.serialization.json.JsonObject

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

    suspend fun uploadSingleImage(
        session: UserSession,
        patientId: Int,
        examType: String,
        fileName: String,
        bytes: ByteArray,
        mimeType: String,
        description: String? = null,
    ): AppResult<Pair<UserSession, JsonObject>> {
        return withRefresh(session) { activeSession ->
            val requestUrl = "${apiClient.baseUrl}/upload/single"
            try {
                val envelope = apiClient.httpClient.post(requestUrl) {
                    header(HttpHeaders.Authorization, "Bearer ${activeSession.accessToken}")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("patient_id", patientId.toString())
                                append("exam_type", examType)
                                if (!description.isNullOrBlank()) {
                                    append("description", description.trim())
                                }
                                append(
                                    key = "file",
                                    value = bytes,
                                    headers = Headers.build {
                                        append(HttpHeaders.ContentType, mimeType)
                                        append(HttpHeaders.ContentDisposition, """form-data; name="file"; filename="$fileName"""")
                                    },
                                )
                            },
                        ),
                    )
                }.body<ApiEnvelope<JsonObject>>()
                val payload = envelope.data
                if (payload == null) {
                    AppResult.Failure(
                        message = envelope.message,
                        code = envelope.code,
                        isUnauthorized = envelope.code == HttpStatusCode.Unauthorized.value,
                        debugDetails = "[POST] $requestUrl code=${envelope.code} data=null",
                    )
                } else {
                    AppResult.Success(payload)
                }
            } catch (e: ClientRequestException) {
                val status = e.response.status
                AppResult.Failure(
                    message = "上传影像失败",
                    code = status.value,
                    isUnauthorized = status == HttpStatusCode.Unauthorized,
                    debugDetails = "[POST] $requestUrl status=${status.value}",
                )
            } catch (e: Exception) {
                AppResult.Failure(
                    message = apiClient.classifyNetworkError(e.message),
                    debugDetails = "[POST] $requestUrl message=${e.message ?: "N/A"}",
                )
            }
        }
    }

    suspend fun deleteImageFile(
        session: UserSession,
        imageId: Int,
    ): AppResult<Pair<UserSession, Unit>> {
        return withRefresh(session) { activeSession ->
            val requestUrl = "${apiClient.baseUrl}/image-files/$imageId"
            try {
                val response = apiClient.httpClient.delete(requestUrl) {
                    header(HttpHeaders.Authorization, "Bearer ${activeSession.accessToken}")
                }
                val ok = response.status.value in 200..299
                if (ok) {
                    AppResult.Success(Unit)
                } else {
                    val bodyText = runCatching { response.bodyAsText() }.getOrNull()
                    AppResult.Failure(
                        message = "删除影像失败",
                        code = response.status.value,
                        isUnauthorized = response.status == HttpStatusCode.Unauthorized,
                        debugDetails = "[DELETE] $requestUrl status=${response.status.value} body=${bodyText ?: "N/A"}",
                    )
                }
            } catch (e: ClientRequestException) {
                val status = e.response.status
                AppResult.Failure(
                    message = "删除影像失败",
                    code = status.value,
                    isUnauthorized = status == HttpStatusCode.Unauthorized,
                    debugDetails = "[DELETE] $requestUrl status=${status.value}",
                )
            } catch (e: Exception) {
                AppResult.Failure(
                    message = apiClient.classifyNetworkError(e.message),
                    debugDetails = "[DELETE] $requestUrl message=${e.message ?: "N/A"}",
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
