package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.DashboardOverview
import com.xiehe.spine.data.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = false,
    val data: DashboardOverview? = null,
    val errorMessage: String? = null,
)

class DashboardViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun load(
        session: UserSession,
        repository: DashboardRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.loadOverview(session)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            loading = false,
                            data = result.data.second,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(loading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
