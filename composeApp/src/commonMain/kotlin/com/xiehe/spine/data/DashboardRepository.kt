package com.xiehe.spine.data

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession

class DashboardRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadOverview(session: UserSession): AppResult<Pair<UserSession, DashboardOverview>> {
        val direct = apiClient.get<DashboardOverview>(
            path = "/dashboard/overview",
            accessToken = session.accessToken,
        )
        if (direct is AppResult.Success) {
            return AppResult.Success(session to direct.data)
        }
        if (direct is AppResult.Failure && direct.isUnauthorized) {
            return when (val refreshed = authRepository.refreshToken(session)) {
                is AppResult.Success -> {
                    when (
                        val retried = apiClient.get<DashboardOverview>(
                            path = "/dashboard/overview",
                            accessToken = refreshed.data.accessToken,
                        )
                    ) {
                        is AppResult.Success -> AppResult.Success(refreshed.data to retried.data)
                        is AppResult.Failure -> retried
                    }
                }

                is AppResult.Failure -> refreshed
            }
        }
        return direct as AppResult.Failure
    }
}
