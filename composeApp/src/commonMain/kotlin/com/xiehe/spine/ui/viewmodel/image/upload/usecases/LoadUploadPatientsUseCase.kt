package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.patient.PatientRepository
import com.xiehe.spine.data.patient.PatientSummary

class LoadUploadPatientsUseCase {
    suspend operator fun invoke(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ): LoadUploadPatientsOutcome {
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

                is AppResult.Failure -> return LoadUploadPatientsOutcome.Failure(result)
            }
        }

        return LoadUploadPatientsOutcome.Success(aggregate)
    }
}

sealed interface LoadUploadPatientsOutcome {
    data class Success(val patients: List<PatientSummary>) : LoadUploadPatientsOutcome
    data class Failure(val error: AppResult.Failure) : LoadUploadPatientsOutcome
}
