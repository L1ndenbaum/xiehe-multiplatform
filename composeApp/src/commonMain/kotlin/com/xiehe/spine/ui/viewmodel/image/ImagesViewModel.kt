package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.notifySessionExpired
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.image.ImageCategory
import com.xiehe.spine.data.image.ImageWorkflowStatus
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.image.ImageFileSummary
import com.xiehe.spine.data.image.normalizeImageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ImageTypeFilter(
    val label: String,
    val category: ImageCategory? = null,
) {
    ALL("全部类型"),
    FRONT("正位X光片", ImageCategory.FRONT),
    SIDE("侧位X光片", ImageCategory.SIDE),
    LEFT_BENDING("左侧曲位", ImageCategory.LEFT_BENDING),
    RIGHT_BENDING("右侧曲位", ImageCategory.RIGHT_BENDING),
    POSTURE_PHOTO("体态照片", ImageCategory.POSTURE_PHOTO),
}

enum class ImageStatusFilter(val label: String) {
    ALL("全部状态"),
    PENDING_REVIEW("待审核"),
    ARCHIVED("已归档"),
    PROCESSING("处理中"),
    FAILED("失败"),
}

data class ImagesUiState(
    val loading: Boolean = false,
    val lastLoadedAtEpochSeconds: Long? = null,
    val search: String = "",
    val typeFilter: ImageTypeFilter = ImageTypeFilter.ALL,
    val statusFilter: ImageStatusFilter = ImageStatusFilter.ALL,
    val items: List<ImageFileSummary> = emptyList(),
    val filteredItems: List<ImageFileSummary> = emptyList(),
    val summaryTotalCount: Int = 0,
    val summaryReviewedCount: Int = 0,
    val errorMessage: String? = null,
)

class ImagesViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(ImagesUiState())
    val state: StateFlow<ImagesUiState> = _state.asStateFlow()

    fun updateSearch(value: String) {
        _state.update { current ->
            val next = current.copy(search = value)
            next.copy(filteredItems = applyFilters(next))
        }
    }

    fun updateTypeFilter(value: ImageTypeFilter) {
        _state.update { current ->
            val next = current.copy(typeFilter = value)
            next.copy(filteredItems = applyFilters(next))
        }
    }

    fun updateStatusFilter(value: ImageStatusFilter) {
        _state.update { current ->
            val next = current.copy(statusFilter = value)
            next.copy(filteredItems = applyFilters(next))
        }
    }

    fun refreshIfNeeded(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
        force: Boolean = false,
        onSessionExpired: (String) -> Unit = {},
    ) {
        val snapshot = _state.value
        if (snapshot.loading) {
            return
        }
        if (!force && snapshot.items.isNotEmpty()) {
            val now = currentEpochSeconds()
            val last = snapshot.lastLoadedAtEpochSeconds ?: 0L
            if ((now - last) < 20L) {
                return
            }
        }
        refresh(session, repository, onSessionUpdated, onSessionExpired)
    }

    fun refresh(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit = {},
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.loadImageFiles(session)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    val payload = result.data.second.items
                    _state.update { current ->
                        val next = current.copy(
                            loading = false,
                            lastLoadedAtEpochSeconds = currentEpochSeconds(),
                            items = payload,
                            summaryTotalCount = payload.size,
                            summaryReviewedCount = payload.count { image ->
                                normalizeImageStatus(image.status) == ImageWorkflowStatus.PROCESSED
                            },
                            errorMessage = null,
                        )
                        next.copy(filteredItems = applyFilters(next))
                    }
                }

                is AppResult.Failure -> {
                    if (result.notifySessionExpired(onSessionExpired)) {
                        _state.update { it.copy(loading = false, errorMessage = null) }
                    } else {
                        _state.update {
                            it.copy(
                                loading = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun syncReviewSummary(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit = {},
    ) {
        scope.launch {
            when (val result = repository.loadAllImageFiles(session)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    val images = result.data.second
                    _state.update {
                        it.copy(
                            summaryTotalCount = images.size,
                            summaryReviewedCount = images.count { image ->
                                normalizeImageStatus(image.status) == ImageWorkflowStatus.PROCESSED
                            },
                        )
                    }
                }

                is AppResult.Failure -> {
                    result.notifySessionExpired(onSessionExpired)
                }
            }
        }
    }

    private fun applyFilters(state: ImagesUiState): List<ImageFileSummary> {
        val keyword = state.search.trim().lowercase()
        return state.items.filter { item ->
            val matchesSearch = keyword.isBlank() || item.matchesSearch(keyword)
            val matchesType = state.typeFilter.category?.let {
                item.description?.trim() == state.typeFilter.label
            } ?: true
            val matchesStatus = when (state.statusFilter) {
                ImageStatusFilter.ALL -> true
                ImageStatusFilter.PENDING_REVIEW -> normalizeImageStatus(item.status) == ImageWorkflowStatus.UPLOADED
                ImageStatusFilter.ARCHIVED -> normalizeImageStatus(item.status) == ImageWorkflowStatus.PROCESSED
                ImageStatusFilter.PROCESSING -> normalizeImageStatus(item.status) == ImageWorkflowStatus.PROCESSING
                ImageStatusFilter.FAILED -> normalizeImageStatus(item.status) == ImageWorkflowStatus.FAILED
            }
            matchesSearch && matchesType && matchesStatus
        }
    }

    private fun ImageFileSummary.matchesSearch(keyword: String): Boolean {
        return listOfNotNull(
            patientName,
            patientId?.toString(),
            originalFilename,
            description,
            modality,
            bodyPart,
            uploaderName,
        ).any { value ->
            value.lowercase().contains(keyword)
        }
    }
}
