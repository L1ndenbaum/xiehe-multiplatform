package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.image.ImageFileSummary
import com.xiehe.spine.data.image.ImageWorkflowStatus
import com.xiehe.spine.data.image.normalizeImageStatus

class RefreshImagesUseCase {
    suspend operator fun invoke(
        session: UserSession,
        repository: ImageFileRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ): RefreshImagesOutcome {
        return when (val result = repository.loadImageFiles(session)) {
            is AppResult.Success -> {
                val updatedSession = result.data.first
                onSessionUpdated(updatedSession)
                val items = result.data.second.items
                RefreshImagesOutcome.Success(
                    items = items,
                    filteredItems = ImageFilterPolicy.apply(ImagesUiState(items = items)),
                    summaryTotalCount = items.size,
                    summaryReviewedCount = items.count { image ->
                        normalizeImageStatus(image.status) == ImageWorkflowStatus.PROCESSED
                    },
                    lastLoadedAtEpochSeconds = currentEpochSeconds(),
                )
            }

            is AppResult.Failure -> RefreshImagesOutcome.Failure(result)
        }
    }
}

sealed interface RefreshImagesOutcome {
    data class Success(
        val items: List<ImageFileSummary>,
        val filteredItems: List<ImageFileSummary>,
        val summaryTotalCount: Int,
        val summaryReviewedCount: Int,
        val lastLoadedAtEpochSeconds: Long,
    ) : RefreshImagesOutcome

    data class Failure(val error: AppResult.Failure) : RefreshImagesOutcome
}
