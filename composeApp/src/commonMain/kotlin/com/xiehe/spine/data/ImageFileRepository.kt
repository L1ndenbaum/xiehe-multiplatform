package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.cache.ImageCacheRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject

class ImageFileRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
    private val cacheRepository: ImageCacheRepository,
) {
    private val fileLocks = mutableMapOf<Int, Mutex>()
    private val fileLocksGuard = Mutex()
    private val memoryCacheGuard = Mutex()
    private val memoryImageCache = linkedMapOf<Int, ByteArray>()
    private val maxMemoryEntries = 24

    suspend fun loadImageFiles(session: UserSession): AppResult<Pair<UserSession, ImageFilePageData>> {
        return when (
            val result = withRefresh(session) { activeSession ->
                apiClient.get<ImageFilePageData>(path = "/image-files", accessToken = activeSession.accessToken)
            }
        ) {
            is AppResult.Success -> {
                var activeSession = result.data.first
                var mergedItems = withPatientNameCache(result.data.second.items)

                val unresolvedIds = mergedItems
                    .asSequence()
                    .filter { it.patientId != null && it.patientName.isNullOrBlank() }
                    .mapNotNull { it.patientId }
                    .toSet()
                if (unresolvedIds.isNotEmpty()) {
                    val resolvedById = resolvePatientNamesByIds(activeSession, unresolvedIds)
                    activeSession = resolvedById.first
                    if (resolvedById.second.isNotEmpty()) {
                        cacheRepository.putPatientNameMap(resolvedById.second)
                        mergedItems = mergedItems.map { item ->
                            val patientId = item.patientId
                            if (patientId == null || !item.patientName.isNullOrBlank()) {
                                item
                            } else {
                                val resolvedName = resolvedById.second[patientId]
                                if (resolvedName.isNullOrBlank()) item else item.copy(patientName = resolvedName)
                            }
                        }
                    }
                }

                cacheRepository.mergeImageItems(mergedItems)
                cacheRepository.putPatientNameMap(
                    mergedItems
                        .mapNotNull { item ->
                            val patientId = item.patientId ?: return@mapNotNull null
                            val patientName = item.patientName?.trim().orEmpty()
                            if (patientName.isBlank()) null else (patientId to patientName)
                        }
                        .toMap(),
                )
                val payload = result.data.second.copy(items = mergedItems, fromCache = false)
                AppResult.Success(activeSession to payload)
            }

            is AppResult.Failure -> {
                val cachedItems = cacheRepository.getImageListSnapshot().orEmpty()
                if (cachedItems.isEmpty() || result.isUnauthorized) {
                    result
                } else {
                    AppResult.Success(
                        session to ImageFilePageData(
                            items = cachedItems,
                            pagination = Pagination(
                                total = cachedItems.size,
                                page = 1,
                                pageSize = cachedItems.size.coerceAtLeast(1),
                                totalPages = 1,
                            ),
                            fromCache = true,
                        ),
                    )
                }
            }
        }
    }

    suspend fun loadAllImageFiles(session: UserSession): AppResult<Pair<UserSession, List<ImageFileSummary>>> {
        var activeSession = session
        var page = 1
        var totalPages = 1
        val aggregate = linkedMapOf<Int, ImageFileSummary>()

        while (page <= totalPages) {
            val path = "/image-files?page=$page&page_size=50"
            when (
                val result = withRefresh(activeSession) { candidate ->
                    apiClient.get<ImageFilePageData>(path = path, accessToken = candidate.accessToken)
                }
            ) {
                is AppResult.Success -> {
                    activeSession = result.data.first
                    val payload = result.data.second
                    payload.items.forEach { aggregate[it.id] = it }
                    totalPages = payload.pagination.totalPages.coerceAtLeast(1)
                    page += 1
                }

                is AppResult.Failure -> {
                    val fallback = cacheRepository.getImageListSnapshot().orEmpty()
                    return if (fallback.isNotEmpty() && !result.isUnauthorized) {
                        AppResult.Success(activeSession to fallback)
                    } else {
                        result
                    }
                }
            }
        }

        val merged = withPatientNameCache(aggregate.values.toList())
        cacheRepository.putImageListSnapshot(merged)
        cacheRepository.putPatientNameMap(
            merged
                .mapNotNull { item ->
                    val patientId = item.patientId ?: return@mapNotNull null
                    val patientName = item.patientName?.trim().orEmpty()
                    if (patientName.isBlank()) null else (patientId to patientName)
                }
                .toMap(),
        )
        return AppResult.Success(activeSession to merged)
    }

    suspend fun downloadImageBytes(
        session: UserSession,
        fileId: Int,
    ): AppResult<Pair<UserSession, ByteArray>> {
        getMemoryBytes(fileId)?.let { cached ->
            return AppResult.Success(session to cached)
        }
        cacheRepository.getImageBytes(fileId)?.let { cached ->
            putMemoryBytes(fileId, cached)
            return AppResult.Success(session to cached)
        }

        val fileLock = lockForFile(fileId)
        return fileLock.withLock {
            cacheRepository.getImageBytes(fileId)?.let { cached ->
                putMemoryBytes(fileId, cached)
                return@withLock AppResult.Success(session to cached)
            }

            when (
                val network = withRefresh(session) { activeSession ->
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
            ) {
                is AppResult.Success -> {
                    putMemoryBytes(fileId, network.data.second)
                    cacheRepository.putImageBytes(
                        fileId = fileId,
                        bytes = network.data.second,
                        mimeType = null,
                        fileName = null,
                    )
                    AppResult.Success(network.data)
                }

                is AppResult.Failure -> network
            }
        }
    }

    suspend fun evictImageCache(fileId: Int) {
        removeMemoryBytes(fileId)
        cacheRepository.removeImage(fileId)
    }

    suspend fun getCachedPatientName(patientId: Int): String? {
        return cacheRepository.getPatientNameById(patientId)
    }

    suspend fun getCachedImageList(): List<ImageFileSummary> {
        return cacheRepository.getImageListSnapshot().orEmpty()
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
        val safeDescription = examType.trim().ifBlank { description?.trim().orEmpty() }
        return withRefresh(session) { activeSession ->
            val requestUrl = "${apiClient.baseUrl}/upload/single"
            try {
                val envelope = apiClient.httpClient.post(requestUrl) {
                    header(HttpHeaders.Authorization, "Bearer ${activeSession.accessToken}")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("patient_id", patientId.toString())
                                // Keep this field for compatibility with older server variants.
                                append("exam_type", examType)
                                if (safeDescription.isNotBlank()) {
                                    append("description", safeDescription)
                                }
                                append(
                                    key = "file",
                                    value = bytes,
                                    headers = Headers.build {
                                        append(HttpHeaders.ContentType, mimeType)
                                        append(HttpHeaders.ContentDisposition, """form-data; name=\"file\"; filename=\"$fileName\"""")
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
        return when (
            val result = withRefresh(session) { activeSession ->
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
        ) {
            is AppResult.Success -> {
                removeMemoryBytes(imageId)
                cacheRepository.removeImage(imageId)
                cacheRepository.removeImageItem(imageId)
                AppResult.Success(result.data)
            }

            is AppResult.Failure -> result
        }
    }

    private suspend fun withPatientNameCache(
        items: List<ImageFileSummary>,
    ): List<ImageFileSummary> {
        if (items.isEmpty()) {
            return items
        }
        return items.map { item ->
            val currentName = item.patientName?.trim().orEmpty()
            if (currentName.isNotBlank()) {
                item
            } else {
                val cachedName = item.patientId?.let { cacheRepository.getPatientNameById(it) }
                if (cachedName.isNullOrBlank()) item else item.copy(patientName = cachedName)
            }
        }
    }

    private suspend fun resolvePatientNamesByIds(
        session: UserSession,
        patientIds: Set<Int>,
    ): Pair<UserSession, Map<Int, String>> {
        if (patientIds.isEmpty()) {
            return session to emptyMap()
        }
        var activeSession = session
        val resolved = linkedMapOf<Int, String>()
        patientIds.forEach { patientId ->
            val result = withRefresh(activeSession) { candidate ->
                apiClient.get<PatientDetail>(
                    path = "/patients/$patientId",
                    accessToken = candidate.accessToken,
                )
            }
            if (result is AppResult.Success) {
                activeSession = result.data.first
                val name = result.data.second.name.trim()
                if (name.isNotBlank()) {
                    resolved[patientId] = name
                }
            }
        }
        return activeSession to resolved
    }

    private suspend fun lockForFile(fileId: Int): Mutex {
        fileLocksGuard.withLock {
            return fileLocks.getOrPut(fileId) { Mutex() }
        }
    }

    private suspend fun getMemoryBytes(fileId: Int): ByteArray? {
        return memoryCacheGuard.withLock {
            memoryImageCache[fileId]
        }
    }

    private suspend fun putMemoryBytes(fileId: Int, bytes: ByteArray) {
        memoryCacheGuard.withLock {
            memoryImageCache.remove(fileId)
            memoryImageCache[fileId] = bytes
            while (memoryImageCache.size > maxMemoryEntries) {
                val eldest = memoryImageCache.entries.firstOrNull()?.key ?: break
                memoryImageCache.remove(eldest)
            }
        }
    }

    private suspend fun removeMemoryBytes(fileId: Int) {
        memoryCacheGuard.withLock {
            memoryImageCache.remove(fileId)
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
