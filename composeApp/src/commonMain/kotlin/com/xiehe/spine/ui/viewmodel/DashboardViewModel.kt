package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.data.DashboardOverview
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = false,
    val data: DashboardOverview? = null,
    val pendingItems: List<ImageFileSummary> = emptyList(),
    val doctorDisplayName: String = "",
    val errorMessage: String? = null,
)

class DashboardViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun load(
        session: UserSession,
        dashboardRepository: DashboardRepository,
        imageRepository: ImageFileRepository,
        authRepository: AuthRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            val overviewDeferred = async { dashboardRepository.loadOverview(session) }
            val imagesDeferred = async { imageRepository.loadAllImageFiles(session) }
            val meDeferred = async { authRepository.getCurrentUser(session) }

            val overviewResult = overviewDeferred.await()
            val imagesResult = imagesDeferred.await()
            val meResult = meDeferred.await()

            when (overviewResult) {
                is AppResult.Success -> {
                    val latestSession = listOfNotNull(
                        overviewResult.data.first,
                        (imagesResult as? AppResult.Success)?.data?.first,
                        (meResult as? AppResult.Success)?.data?.first,
                    ).lastOrNull() ?: session
                    onSessionUpdated(latestSession)

                    val pendingItems = when (imagesResult) {
                        is AppResult.Success -> {
                            imagesResult.data.second.filter { image ->
                                val status = image.status?.uppercase()
                                status == "UPLOADED" || status == "PROCESSING"
                            }
                        }

                        else -> emptyList()
                    }
                    val doctorName = when (meResult) {
                        is AppResult.Success -> {
                            meResult.data.second.realName
                                ?: meResult.data.second.fullName
                                ?: meResult.data.second.username
                        }

                        else -> latestSession.fullName ?: latestSession.username
                    }.orEmpty()
                    _state.update {
                        it.copy(
                            loading = false,
                            data = overviewResult.data.second,
                            pendingItems = pendingItems,
                            doctorDisplayName = doctorName,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    val fallbackPending = when (imagesResult) {
                        is AppResult.Success -> imagesResult.data.second.filter { image ->
                            val status = image.status?.uppercase()
                            status == "UPLOADED" || status == "PROCESSING"
                        }

                        else -> emptyList()
                    }
                    _state.update {
                        it.copy(
                            loading = false,
                            pendingItems = fallbackPending,
                            errorMessage = overviewResult.message,
                        )
                    }
                }
            }
        }
    }
}
