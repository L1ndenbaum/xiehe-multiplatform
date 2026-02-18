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
import kotlin.time.TimeSource

class ApiClient(
    @PublishedApi internal val httpClient: HttpClient,
    @PublishedApi internal val baseUrl: String,
    @PublishedApi internal val enableDiagnostics: Boolean = false,
) {
    internal suspend inline fun <reified T> get(
        path: String,
        accessToken: String? = null,
    ): AppResult<T> {
        val requestUrl = "$baseUrl$path"
        return request(
            requestName = "GET",
            requestUrl = requestUrl,
        ) {
            get {
                url(requestUrl)
                attachAuth(accessToken)
            }
        }
    }

    internal suspend inline fun <reified T, reified B : Any> post(
        path: String,
        body: B,
        accessToken: String? = null,
    ): AppResult<T> {
        val requestUrl = "$baseUrl$path"
        return request(
            requestName = "POST",
            requestUrl = requestUrl,
        ) {
            post {
                url(requestUrl)
                attachAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }

    internal suspend inline fun <reified T> request(
        requestName: String,
        requestUrl: String,
        crossinline block: suspend HttpClient.() -> io.ktor.client.statement.HttpResponse,
    ): AppResult<T> {
        val mark = TimeSource.Monotonic.markNow()
        if (enableDiagnostics) {
            println("SpineNetwork [$requestName] START $requestUrl")
        }
        return try {
            val response = httpClient.block()
            val envelope = response.body<ApiEnvelope<T>>()
            val payload = envelope.data
            if (payload == null) {
                val unauthorizedByEnvelope = envelope.code == HttpStatusCode.Unauthorized.value
                val result = AppResult.Failure(
                    message = envelope.message,
                    code = envelope.code,
                    isUnauthorized = unauthorizedByEnvelope,
                    debugDetails = "[$requestName] $requestUrl data is null; envelopeCode=${envelope.code}",
                )
                logDone(mark, requestName, requestUrl, "failure(code=${envelope.code})")
                result
            } else {
                logDone(mark, requestName, requestUrl, "success")
                AppResult.Success(payload)
            }
        } catch (e: ClientRequestException) {
            val status = e.response.status
            val error = runCatching { e.response.body<ApiErrorEnvelope>() }.getOrNull()
            val result = AppResult.Failure(
                message = error?.message ?: "请求失败",
                code = status.value,
                isUnauthorized = status == HttpStatusCode.Unauthorized,
                debugDetails = buildString {
                    append("[$requestName] ")
                    append(requestUrl)
                    append(" status=")
                    append(status.value)
                    append(" errorCode=")
                    append(error?.errorCode ?: "N/A")
                },
            )
            logDone(mark, requestName, requestUrl, "http-error(status=${status.value})")
            result
        } catch (e: Exception) {
            val exceptionType = e::class.simpleName ?: "Exception"
            val details = "[$requestName] $requestUrl type=$exceptionType message=${e.message ?: "N/A"}"
            val userMessage = classifyNetworkError(e.message)
            val result = AppResult.Failure(
                message = userMessage,
                debugDetails = details,
            )
            logDone(mark, requestName, requestUrl, "exception(type=$exceptionType)")
            result
        }
    }

    @PublishedApi
    internal fun HttpRequestBuilder.attachAuth(accessToken: String?) {
        if (!accessToken.isNullOrBlank()) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    @PublishedApi
    internal fun logDone(
        mark: kotlin.time.TimeMark,
        requestName: String,
        requestUrl: String,
        outcome: String,
    ) {
        if (enableDiagnostics) {
            println("SpineNetwork [$requestName] END $requestUrl outcome=$outcome in ${mark.elapsedNow().inWholeMilliseconds}ms")
        }
    }

    @PublishedApi
    internal fun classifyNetworkError(rawMessage: String?): String {
        val message = rawMessage ?: return "网络异常"
        return when {
            "ECONNREFUSED" in message || "Connection refused" in message ->
                "连接被拒绝，请确认后端服务已启动并允许访问 115.190.121.59:8080"

            "CLEARTEXT" in message || "Cleartext" in message ->
                "明文 HTTP 被系统阻止，请检查当前构建是否允许 cleartext"

            "EPERM" in message ->
                "系统拒绝了网络访问，请检查应用网络权限与系统网络策略"

            "ENETUNREACH" in message || "Network is unreachable" in message ->
                "网络不可达，请检查手机网络或 VPN/代理设置"

            "ETIMEDOUT" in message || "timeout" in message.lowercase() ->
                "连接超时，请稍后重试"

            else -> message
        }
    }
}
