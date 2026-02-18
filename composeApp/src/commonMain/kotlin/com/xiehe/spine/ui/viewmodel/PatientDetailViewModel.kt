package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
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
    val errorMessage: String? = null,
)

class PatientDetailViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(PatientDetailUiState())
    val state: StateFlow<PatientDetailUiState> = _state.asStateFlow()

    fun load(
        patientId: Int,
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.loadPatientDetail(session, patientId)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update { it.copy(loading = false, detail = result.data.second) }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(loading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
