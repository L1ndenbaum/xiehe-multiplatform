package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.notifySessionExpired
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.image.ImageCategory
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.patient.PatientRepository
import com.xiehe.spine.data.patient.PatientSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadFilePayload(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

data class ImageUploadUiState(
    val loadingPatients: Boolean = false,
    val uploading: Boolean = false,
    val patients: List<PatientSummary> = emptyList(),
    val selectedPatientId: Int? = null,
    val selectedExamType: ImageCategory = ImageCategory.FRONT,
    val examTypes: List<ImageCategory> = ImageCategory.entries,
    val selectedFile: UploadFilePayload? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class ImageUploadViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(ImageUploadUiState())
    val state: StateFlow<ImageUploadUiState> = _state.asStateFlow()

    fun loadPatients(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit = {},
    ) {
        if (_state.value.loadingPatients) {
            return
        }
        scope.launch {
            _state.update { it.copy(loadingPatients = true, errorMessage = null) }
            var activeSession = session
            var page = 1
            var totalPages = 1
            val aggregate = mutableListOf<PatientSummary>()
            while (page <= totalPages) {
                when (
                    val result = repository.loadPatients(
                        session = activeSession,
                        page = page,
                        pageSize = 50,
                        search = "",
                    )
                ) {
                    is AppResult.Success -> {
                        activeSession = result.data.first
                        onSessionUpdated(activeSession)
                        val payload = result.data.second
                        aggregate += payload.items
                        page += 1
                        totalPages = payload.pagination.totalPages.coerceAtLeast(1)
                    }

                    is AppResult.Failure -> {
                        if (result.notifySessionExpired(onSessionExpired)) {
                            _state.update { it.copy(loadingPatients = false, errorMessage = null) }
                        } else {
                            _state.update {
                                it.copy(
                                    loadingPatients = false,
                                    errorMessage = result.message,
                                )
                            }
                        }
                        return@launch
                    }
                }
            }
            _state.update {
                val firstPatientId = aggregate.firstOrNull()?.id
                it.copy(
                    loadingPatients = false,
                    patients = aggregate,
                    selectedPatientId = it.selectedPatientId ?: firstPatientId,
                )
            }
        }
    }

    fun updatePatient(patientId: Int?) {
        _state.update { it.copy(selectedPatientId = patientId, errorMessage = null) }
    }

    fun updateExamType(examType: ImageCategory) {
        _state.update { it.copy(selectedExamType = examType, errorMessage = null) }
    }

    fun setSelectedFile(file: UploadFilePayload?) {
        _state.update { it.copy(selectedFile = file, errorMessage = null) }
    }

    fun submit(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSuccess: () -> Unit,
        onSessionExpired: (String) -> Unit = {},
    ) {
        val current = _state.value
        val patientId = current.selectedPatientId
        val file = current.selectedFile
        if (patientId == null) {
            _state.update { it.copy(errorMessage = "请选择患者") }
            return
        }
        if (file == null) {
            _state.update { it.copy(errorMessage = "请选择要上传的影像文件") }
            return
        }

        scope.launch {
            _state.update { it.copy(uploading = true, errorMessage = null, successMessage = null) }
            when (
                val result = repository.uploadSingleImage(
                    session = session,
                    patientId = patientId,
                    examType = _state.value.selectedExamType.label,
                    fileName = file.name,
                    bytes = file.bytes,
                    mimeType = file.mimeType,
                    description = _state.value.selectedExamType.label,
                )
            ) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            uploading = false,
                            successMessage = "影像上传成功",
                            selectedFile = null,
                        )
                    }
                    onSuccess()
                }

                is AppResult.Failure -> {
                    if (result.notifySessionExpired(onSessionExpired)) {
                        _state.update { it.copy(uploading = false, errorMessage = null) }
                    } else {
                        _state.update {
                            it.copy(
                                uploading = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }
    }
}
