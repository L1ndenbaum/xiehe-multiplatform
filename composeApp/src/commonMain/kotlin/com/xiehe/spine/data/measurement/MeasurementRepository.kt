package com.xiehe.spine.data.measurement

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ApiClient
import com.xiehe.spine.data.auth.AuthRepository

class MeasurementRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadMeasurements(
        session: UserSession,
        imageId: Int,
    ): AppResult<Pair<UserSession, ImageMeasurementsData>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/measurements/$imageId", accessToken = activeSession.accessToken)
        }
    }

    suspend fun saveMeasurements(
        session: UserSession,
        imageId: Int,
        request: SaveMeasurementsRequest,
    ): AppResult<Pair<UserSession, SaveMeasurementsResult>> {
        return withRefresh(session) { activeSession ->
            apiClient.post(path = "/measurements/$imageId", body = request, accessToken = activeSession.accessToken)
        }
    }

    suspend fun generateReport(
        session: UserSession,
        request: GenerateReportRequest,
    ): AppResult<Pair<UserSession, GenerateReportResult>> {
        return withRefresh(session) { activeSession ->
            apiClient.post(path = "/report-generation/generate", body = request, accessToken = activeSession.accessToken)
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
