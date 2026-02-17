package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class ApiClient(
    @PublishedApi internal val httpClient: HttpClient,
    @PublishedApi internal val baseUrl: String,
) {
    internal suspend inline fun <reified T> get(
        path: String,
        accessToken: String? = null,
    ): AppResult<T> {
        return request {
            get {
                url("$baseUrl$path")
                attachAuth(accessToken)
            }
        }
    }

    internal suspend inline fun <reified T, reified B : Any> post(
        path: String,
        body: B,
        accessToken: String? = null,
    ): AppResult<T> {
        return request {
            post {
                url("$baseUrl$path")
                attachAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }

    internal suspend inline fun <reified T> request(
        crossinline block: suspend HttpClient.() -> io.ktor.client.statement.HttpResponse,
    ): AppResult<T> {
        return try {
            val response = httpClient.block()
            val envelope = response.body<ApiEnvelope<T>>()
            val payload = envelope.data
            if (payload == null) {
                AppResult.Failure(message = envelope.message, code = envelope.code)
            } else {
                AppResult.Success(payload)
            }
        } catch (e: ClientRequestException) {
            val status = e.response.status
            val error = runCatching { e.response.body<ApiErrorEnvelope>() }.getOrNull()
            AppResult.Failure(
                message = error?.message ?: "请求失败",
                code = status.value,
                isUnauthorized = status == HttpStatusCode.Unauthorized,
            )
        } catch (e: Exception) {
            AppResult.Failure(message = e.message ?: "网络异常")
        }
    }

    @PublishedApi
    internal fun HttpRequestBuilder.attachAuth(accessToken: String?) {
        if (!accessToken.isNullOrBlank()) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }
}
