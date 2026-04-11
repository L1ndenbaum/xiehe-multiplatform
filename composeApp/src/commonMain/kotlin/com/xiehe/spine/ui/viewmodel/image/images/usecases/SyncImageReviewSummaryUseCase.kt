package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.image.ImageWorkflowStatus
import com.xiehe.spine.data.image.normalizeImageStatus

class SyncImageReviewSummaryUseCase {
    suspend operator fun invoke(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ): SyncImageReviewSummaryOutcome {
        return when (val result = repository.loadAllImageFiles(session)) {
            is AppResult.Success -> {
                val updatedSession = result.data.first
                onSessionUpdated(updatedSession)
                val images = result.data.second
                SyncImageReviewSummaryOutcome.Success(
                    totalCount = images.size,
                    reviewedCount = images.count { image ->
                        normalizeImageStatus(image.status) == ImageWorkflowStatus.PROCESSED
                    },
                )
            }

            is AppResult.Failure -> SyncImageReviewSummaryOutcome.Failure(result)
        }
    }
}

sealed interface SyncImageReviewSummaryOutcome {
    data class Success(
        val totalCount: Int,
        val reviewedCount: Int,
    ) : SyncImageReviewSummaryOutcome

    data class Failure(val error: AppResult.Failure) : SyncImageReviewSummaryOutcome
}
