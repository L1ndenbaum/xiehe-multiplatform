package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ImageTypeFilter(val label: String) {
    ALL("全部类型"),
    XR("X-Ray"),
    CT("CT"),
    MRI("MRI"),
    OTHER("其他"),
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
        refresh(session, repository, onSessionUpdated)
    }

    fun refresh(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
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
                            errorMessage = null,
                        )
                        next.copy(filteredItems = applyFilters(next))
                    }
                }

                is AppResult.Failure -> {
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

    private fun applyFilters(state: ImagesUiState): List<ImageFileSummary> {
        val keyword = state.search.trim().lowercase()
        return state.items.filter { item ->
            val matchesSearch = keyword.isBlank() || item.matchesSearch(keyword)
            val matchesType = when (state.typeFilter) {
                ImageTypeFilter.ALL -> true
                ImageTypeFilter.XR -> item.modality.equals("XR", ignoreCase = true)
                ImageTypeFilter.CT -> item.modality.equals("CT", ignoreCase = true)
                ImageTypeFilter.MRI -> item.modality.equals("MRI", ignoreCase = true)
                ImageTypeFilter.OTHER -> {
                    val modality = item.modality?.uppercase()
                    modality !in setOf("XR", "CT", "MRI")
                }
            }
            val matchesStatus = when (state.statusFilter) {
                ImageStatusFilter.ALL -> true
                ImageStatusFilter.PENDING_REVIEW -> item.status.equals("UPLOADED", ignoreCase = true)
                ImageStatusFilter.ARCHIVED -> item.status.equals("PROCESSED", ignoreCase = true)
                ImageStatusFilter.PROCESSING -> item.status.equals("PROCESSING", ignoreCase = true)
                ImageStatusFilter.FAILED -> item.status.equals("FAILED", ignoreCase = true)
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
