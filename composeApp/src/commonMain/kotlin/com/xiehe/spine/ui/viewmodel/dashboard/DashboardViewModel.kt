package com.xiehe.spine.ui.viewmodel.dashboard
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.data.DashboardOverview
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.data.NotificationMessage
import com.xiehe.spine.data.NotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = false,
    val data: DashboardOverview? = null,
    val pendingItems: List<ImageFileSummary> = emptyList(),
    val recentMessages: List<NotificationMessage> = emptyList(),
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
        notificationRepository: NotificationRepository,
        authRepository: AuthRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            val overviewDeferred = async { dashboardRepository.loadOverview(session) }
            val imagesDeferred = async { imageRepository.loadAllImageFiles(session) }
            val messagesDeferred = async { notificationRepository.loadMessages(session = session, page = 1, pageSize = 4) }
            val meDeferred = async { authRepository.getCurrentUser(session) }

            val overviewResult = overviewDeferred.await()
            val imagesResult = imagesDeferred.await()
            val messagesResult = messagesDeferred.await()
            val meResult = meDeferred.await()

            val latestSession = listOfNotNull(
                (overviewResult as? AppResult.Success)?.data?.first,
                (imagesResult as? AppResult.Success)?.data?.first,
                (messagesResult as? AppResult.Success)?.data?.first,
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
            val recentMessages = when (messagesResult) {
                is AppResult.Success -> messagesResult.data.second.items.take(4)
                else -> emptyList()
            }

            when (overviewResult) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            data = overviewResult.data.second,
                            pendingItems = pendingItems,
                            recentMessages = recentMessages,
                            doctorDisplayName = doctorName,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            pendingItems = pendingItems,
                            recentMessages = recentMessages,
                            doctorDisplayName = doctorName,
                            errorMessage = overviewResult.message,
                        )
                    }
                }
            }
        }
    }
}
