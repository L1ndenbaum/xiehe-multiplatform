package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

private const val defaultAiBaseUrl = "http://115.190.121.59:8001"

class AiInferenceRepository(
    private val httpClient: HttpClient,
    private val aiBaseUrl: String = defaultAiBaseUrl,
) {
    suspend fun detectKeypoints(fileName: String, bytes: ByteArray): AppResult<AiDetectResponse> {
        return postImage(path = "/detect_keypoints", fileName = fileName, bytes = bytes)
    }

    suspend fun predict(fileName: String, bytes: ByteArray): AppResult<AiPredictResponse> {
        return postImage(path = "/predict", fileName = fileName, bytes = bytes)
    }

    private suspend inline fun <reified T> postImage(
        path: String,
        fileName: String,
        bytes: ByteArray,
    ): AppResult<T> {
        val requestUrl = "$aiBaseUrl$path"
        return try {
            val response = httpClient.post(requestUrl) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                                },
                            )
                        },
                    ),
                )
                contentType(ContentType.MultiPart.FormData)
            }
            AppResult.Success(response.body<T>())
        } catch (e: ClientRequestException) {
            val status = e.response.status
            AppResult.Failure(
                message = when (status) {
                    HttpStatusCode.UnprocessableEntity -> "AI服务请求参数缺失（file）"
                    HttpStatusCode.MethodNotAllowed -> "AI服务请求方法错误"
                    else -> "AI服务请求失败"
                },
                code = status.value,
                isUnauthorized = false,
                debugDetails = "[POST] $requestUrl status=${status.value}",
            )
        } catch (e: Exception) {
            AppResult.Failure(
                message = "AI服务不可用：${e.message ?: "unknown"}",
                debugDetails = "[POST] $requestUrl message=${e.message ?: "N/A"}",
            )
        }
    }
}
