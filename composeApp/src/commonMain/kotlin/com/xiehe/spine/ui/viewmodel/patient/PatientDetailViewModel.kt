package com.xiehe.spine.ui.viewmodel.patient

import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.data.PatientDetail
import com.xiehe.spine.data.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientDetailUiState(
    val loading: Boolean = false,
    val detail: PatientDetail? = null,
    val relatedImages: List<ImageFileSummary> = emptyList(),
    val relatedLoading: Boolean = false,
    val errorMessage: String? = null,
)

class PatientDetailViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(PatientDetailUiState())
    val state: StateFlow<PatientDetailUiState> = _state.asStateFlow()

    fun load(
        patientId: Int,
        session: UserSession,
        patientRepository: PatientRepository,
        imageRepository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, relatedLoading = true, errorMessage = null) }
            var activeSession = session

            val detail = when (val result = patientRepository.loadPatientDetail(activeSession, patientId)) {
                is AppResult.Success -> {
                    activeSession = result.data.first
                    onSessionUpdated(activeSession)
                    result.data.second
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(loading = false, relatedLoading = false, errorMessage = result.message) }
                    return@launch
                }
            }

            val relatedImages = when (val imageResult = imageRepository.loadAllImageFiles(activeSession)) {
                is AppResult.Success -> {
                    activeSession = imageResult.data.first
                    onSessionUpdated(activeSession)
                    imageResult.data.second.filter { it.patientId == patientId }
                }

                is AppResult.Failure -> emptyList()
            }

            _state.update {
                it.copy(
                    loading = false,
                    relatedLoading = false,
                    detail = detail,
                    relatedImages = relatedImages,
                    errorMessage = null,
                )
            }
        }
    }

    fun clear() {
        _state.value = PatientDetailUiState()
    }
}
