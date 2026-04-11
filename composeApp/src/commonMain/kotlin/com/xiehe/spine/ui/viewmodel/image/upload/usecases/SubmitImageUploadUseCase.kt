package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.image.ImageFileRepository

class SubmitImageUploadUseCase {
    suspend operator fun invoke(
        session: UserSession,
        state: ImageUploadUiState,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ): SubmitImageUploadOutcome {
        val patientId = state.selectedPatientId ?: return SubmitImageUploadOutcome.Invalid("请选择患者")
        val file = state.selectedFile ?: return SubmitImageUploadOutcome.Invalid("请选择要上传的影像文件")

        return when (
            val result = repository.uploadSingleImage(
                session = session,
                patientId = patientId,
                examType = state.selectedExamType.label,
                fileName = file.name,
                bytes = file.bytes,
                mimeType = file.mimeType,
                description = state.selectedExamType.label,
            )
        ) {
            is AppResult.Success -> {
                onSessionUpdated(result.data.first)
                SubmitImageUploadOutcome.Success
            }

            is AppResult.Failure -> SubmitImageUploadOutcome.Failure(result)
        }
    }
}

sealed interface SubmitImageUploadOutcome {
    data object Success : SubmitImageUploadOutcome
    data class Invalid(val message: String) : SubmitImageUploadOutcome
    data class Failure(val error: AppResult.Failure) : SubmitImageUploadOutcome
}
