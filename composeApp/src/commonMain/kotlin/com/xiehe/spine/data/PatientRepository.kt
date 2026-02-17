package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import kotlinx.serialization.json.JsonObject

class PatientRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadPatients(
        session: UserSession,
        page: Int,
        pageSize: Int,
        search: String,
    ): AppResult<Pair<UserSession, PatientPageData>> {
        val path = buildString {
            append("/patients/?page=")
            append(page)
            append("&page_size=")
            append(pageSize)
            if (search.isNotBlank()) {
                append("&search=")
                append(search)
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
    ): AppResult<Pair<UserSession, JsonObject>> {
        return withRefresh(session) { activeSession ->
            apiClient.post(path = "/patients/", body = request, accessToken = activeSession.accessToken)
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
