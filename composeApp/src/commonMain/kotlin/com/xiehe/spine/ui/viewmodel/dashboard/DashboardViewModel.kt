package com.xiehe.spine.ui.viewmodel.dashboard

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.auth.AuthRepository
import com.xiehe.spine.data.dashboard.DashboardOverview
import com.xiehe.spine.data.dashboard.DashboardRepository
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.image.ImageFileSummary
import com.xiehe.spine.data.image.ImageWorkflowStatus
import com.xiehe.spine.data.image.normalizeImageStatus
import com.xiehe.spine.data.notification.NotificationMessage
import com.xiehe.spine.data.notification.NotificationRepository
import com.xiehe.spine.data.patient.PatientSummary
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardPendingTask(
    val image: ImageFileSummary,
    val patientName: String,
    val patientCode: String,
)

data class DashboardUiState(
    val loading: Boolean = false,
    val data: DashboardOverview? = null,
    val pendingItems: List<DashboardPendingTask> = emptyList(),
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
        preloadedPatients: List<PatientSummary> = emptyList(),
        preloadedImages: List<ImageFileSummary> = emptyList(),
    ) {
        scope.launch {
            val initialTasks = buildPendingTasks(
                images = preloadedImages,
                patients = preloadedPatients,
            )
            val initialOverview = buildOverviewSnapshot(
                patients = preloadedPatients,
                images = preloadedImages,
            )

            _state.update {
                it.copy(
                    loading = true,
                    data = initialOverview ?: it.data,
                    pendingItems = initialTasks,
                    errorMessage = null,
                )
            }

            val overviewDeferred = async { dashboardRepository.loadOverview(session) }
            val imagesDeferred = async {
                if (preloadedImages.isNotEmpty()) {
                    AppResult.Success(session to preloadedImages)
                } else {
                    imageRepository.loadAllImageFiles(session)
                }
            }
            val messagesDeferred = async {
                notificationRepository.loadMessages(session = session, page = 1, pageSize = 4)
            }
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
                    buildPendingTasks(
                        images = imagesResult.data.second,
                        patients = preloadedPatients,
                    )
                }

                else -> initialTasks
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
                            data = it.data ?: initialOverview,
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

    private fun buildPendingTasks(
        images: List<ImageFileSummary>,
        patients: List<PatientSummary>,
    ): List<DashboardPendingTask> {
        val patientsById = patients.associateBy { it.id }
        return images
            .asSequence()
            .filter { image ->
                normalizeImageStatus(image.status) in setOf(
                    ImageWorkflowStatus.UPLOADED,
                    ImageWorkflowStatus.PROCESSING,
                )
            }
            .mapNotNull { image ->
                // Avoid fabricating "患者 27" style entries from orphan image rows.
                val linkedPatient = image.patientId?.let(patientsById::get) ?: return@mapNotNull null
                val patientName = linkedPatient.name.trim()
                    .ifBlank { image.patientName?.trim().orEmpty() }
                    .ifBlank { "未命名患者" }
                val patientCode = linkedPatient.patientId.trim()
                    .ifBlank { "未分配编号" }
                DashboardPendingTask(
                    image = image,
                    patientName = patientName,
                    patientCode = patientCode,
                )
            }
            .sortedWith(
                compareByDescending<DashboardPendingTask> { task ->
                    normalizeImageStatus(task.image.status) == ImageWorkflowStatus.UPLOADED
                }.thenByDescending { task ->
                    task.image.createdAt.orEmpty()
                },
            )
            .toList()
    }

    private fun buildOverviewSnapshot(
        patients: List<PatientSummary>,
        images: List<ImageFileSummary>,
    ): DashboardOverview? {
        if (patients.isEmpty() && images.isEmpty()) {
            return null
        }
        val totalImages = images.size
        val pendingImages = images.count { image ->
            normalizeImageStatus(image.status) in setOf(
                ImageWorkflowStatus.UPLOADED,
                ImageWorkflowStatus.PROCESSING,
            )
        }
        val processedImages = images.count { image ->
            normalizeImageStatus(image.status) == ImageWorkflowStatus.PROCESSED
        }
        val completionRate = if (totalImages == 0) {
            0.0
        } else {
            (processedImages.toDouble() / totalImages.toDouble()) * 100.0
        }

        return DashboardOverview(
            totalPatients = patients.size,
            newPatientsToday = 0,
            newPatientsWeek = 0,
            activePatients = patients.size,
            totalImages = totalImages,
            imagesToday = 0,
            imagesWeek = 0,
            pendingImages = pendingImages,
            processedImages = processedImages,
            completionRate = completionRate,
            averageProcessingTime = 0.0,
            systemAlerts = 0,
        )
    }
}

