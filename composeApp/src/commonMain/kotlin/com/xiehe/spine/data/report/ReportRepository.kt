package com.xiehe.spine.data.report

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ApiClient
import com.xiehe.spine.data.auth.AuthRepository

class ReportRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadReports(
        session: UserSession,
        page: Int = 1,
        pageSize: Int = 20,
        patientId: Int? = null,
        status: String? = null,
        priority: String? = null,
        search: String? = null,
    ): AppResult<Pair<UserSession, ReportPageData>> {
        val params = buildList {
            add("page=$page")
            add("page_size=$pageSize")
            patientId?.let { add("patient_id=$it") }
            status?.trim()?.takeIf { it.isNotBlank() }?.let { add("status=$it") }
            priority?.trim()?.takeIf { it.isNotBlank() }?.let { add("priority=$it") }
            search?.trim()?.takeIf { it.isNotBlank() }?.let { add("search=$it") }
        }
        val path = buildString {
            append("/reports/")
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
        return withRefresh(session) { activeSession ->
            apiClient.get(path = path, accessToken = activeSession.accessToken)
        }
    }

    suspend fun getReportDetail(
        session: UserSession,
        reportId: Int,
    ): AppResult<Pair<UserSession, ReportDetail>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/reports/$reportId", accessToken = activeSession.accessToken)
        }
    }

    suspend fun createReport(
        session: UserSession,
        request: ReportCreateRequest,
    ): AppResult<Pair<UserSession, ReportDetail>> {
        return withRefresh(session) { activeSession ->
            apiClient.post(path = "/reports/", body = request, accessToken = activeSession.accessToken)
        }
    }

    suspend fun updateReport(
        session: UserSession,
        reportId: Int,
        request: ReportUpdateRequest,
    ): AppResult<Pair<UserSession, ReportDetail>> {
        return withRefresh(session) { activeSession ->
            apiClient.put(path = "/reports/$reportId", body = request, accessToken = activeSession.accessToken)
        }
    }

    suspend fun deleteReport(
        session: UserSession,
        reportId: Int,
    ): AppResult<Pair<UserSession, String>> {
        return withRefresh(session) { activeSession ->
            apiClient.deleteForMessage(path = "/reports/$reportId", accessToken = activeSession.accessToken)
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
