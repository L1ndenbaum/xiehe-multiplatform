package com.xiehe.spine.data.patient

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ApiClient
import com.xiehe.spine.data.auth.AuthRepository
import com.xiehe.spine.data.cache.ImageCacheRepository
import io.ktor.http.encodeURLParameter

class PatientRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
    private val imageCacheRepository: ImageCacheRepository,
) {
    suspend fun loadAllPatients(session: UserSession): AppResult<Pair<UserSession, List<PatientSummary>>> {
        var activeSession = session
        var page = 1
        var totalPages = 1
        val aggregate = mutableListOf<PatientSummary>()

        while (page <= totalPages) {
            when (
                val result = loadPatients(
                    session = activeSession,
                    page = page,
                    pageSize = 50,
                    search = "",
                )
            ) {
                is AppResult.Success -> {
                    activeSession = result.data.first
                    val payload = result.data.second
                    aggregate += payload.items
                    totalPages = payload.pagination.totalPages.coerceAtLeast(1)
                    page += 1
                }

                is AppResult.Failure -> return result
            }
        }

        return AppResult.Success(activeSession to aggregate)
    }

    suspend fun loadPatients(
        session: UserSession,
        page: Int,
        pageSize: Int,
        search: String,
        gender: String? = null,
        ageMin: Int? = null,
        ageMax: Int? = null,
        status: String? = null,
    ): AppResult<Pair<UserSession, PatientPageData>> {
        val path = buildString {
            append("/patients/?page=")
            append(page)
            append("&page_size=")
            append(pageSize)
            if (search.isNotBlank()) {
                append("&search=")
                append(search.encodeURLParameter())
            }
            if (!gender.isNullOrBlank()) {
                append("&gender=")
                append(gender)
            }
            if (ageMin != null) {
                append("&age_min=")
                append(ageMin)
            }
            if (ageMax != null) {
                append("&age_max=")
                append(ageMax)
            }
            if (!status.isNullOrBlank()) {
                append("&status=")
                append(status.encodeURLParameter())
            }
        }
        return withRefresh(session) { activeSession ->
            apiClient.get(path = path, accessToken = activeSession.accessToken)
        }
    }

    suspend fun loadPatientDetail(
        session: UserSession,
        patientId: Int,
    ): AppResult<Pair<UserSession, PatientDetail>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/patients/$patientId", accessToken = activeSession.accessToken)
        }
    }

    suspend fun createPatient(
        session: UserSession,
        request: CreatePatientRequest,
    ): AppResult<Pair<UserSession, PatientDetail>> {
        return when (
            val result = withRefresh(session) { activeSession ->
                apiClient.post<PatientDetail, CreatePatientRequest>(
                    path = "/patients/",
                    body = request,
                    accessToken = activeSession.accessToken,
                )
            }
        ) {
            is AppResult.Success -> {
                imageCacheRepository.syncPatientName(
                    userId = result.data.first.userId,
                    patientId = result.data.second.id,
                    patientName = result.data.second.name.trim(),
                )
                result
            }

            is AppResult.Failure -> result
        }
    }

    suspend fun updatePatient(
        session: UserSession,
        patientId: Int,
        request: UpdatePatientRequest,
    ): AppResult<Pair<UserSession, PatientDetail>> {
        return when (
            val result = withRefresh(session) { activeSession ->
                apiClient.put<PatientDetail, UpdatePatientRequest>(
                    path = "/patients/$patientId",
                    body = request,
                    accessToken = activeSession.accessToken,
                )
            }
        ) {
            is AppResult.Success -> {
                imageCacheRepository.syncPatientName(
                    userId = result.data.first.userId,
                    patientId = patientId,
                    patientName = result.data.second.name.trim(),
                )
                result
            }

            is AppResult.Failure -> result
        }
    }

    suspend fun deletePatient(
        session: UserSession,
        patientId: Int,
    ): AppResult<Pair<UserSession, String>> {
        return when (
            val result = withRefresh(session) { activeSession ->
                apiClient.deleteForMessage(
                    path = "/patients/$patientId",
                    accessToken = activeSession.accessToken,
                )
            }
        ) {
            is AppResult.Success -> {
                imageCacheRepository.removePatient(
                    userId = result.data.first.userId,
                    patientId = patientId,
                )
                result
            }

            is AppResult.Failure -> result
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
