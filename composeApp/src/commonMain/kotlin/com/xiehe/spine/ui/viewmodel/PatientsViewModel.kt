package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.data.PatientSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientsUiState(
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val search: String = "",
    val items: List<PatientSummary> = emptyList(),
    val page: Int = 1,
    val totalPages: Int = 1,
    val errorMessage: String? = null,
)

class PatientsViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(PatientsUiState())
    val state: StateFlow<PatientsUiState> = _state.asStateFlow()

    fun updateSearch(value: String) {
        _state.update { it.copy(search = value) }
    }

    fun refresh(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        loadInternal(
            session = session,
            repository = repository,
            page = 1,
            append = false,
            onSessionUpdated = onSessionUpdated,
        )
    }

    fun loadMore(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val current = _state.value
        if (current.loadingMore || current.loading || current.page >= current.totalPages) {
            return
        }
        loadInternal(
            session = session,
            repository = repository,
            page = current.page + 1,
            append = true,
            onSessionUpdated = onSessionUpdated,
        )
    }

    private fun loadInternal(
        session: UserSession,
        repository: PatientRepository,
        page: Int,
        append: Boolean,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        scope.launch {
            _state.update {
                it.copy(
                    loading = !append,
                    loadingMore = append,
                    errorMessage = null,
                )
            }
            val search = _state.value.search
            when (
                val result = repository.loadPatients(
                    session = session,
                    page = page,
                    pageSize = 10,
                    search = search,
                )
            ) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    val payload = result.data.second
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            items = if (append) it.items + payload.items else payload.items,
                            page = payload.pagination.page,
                            totalPages = payload.pagination.totalPages.coerceAtLeast(1),
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }
}
