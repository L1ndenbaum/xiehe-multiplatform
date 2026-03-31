package com.xiehe.spine.ui.viewmodel.patient

import com.xiehe.spine.notifySessionExpired
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.patient.PatientRepository
import com.xiehe.spine.data.patient.PatientSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GenderFilter(
    val label: String,
    val queryValue: String?,
) {
    ALL("全部性别", null),
    MALE("男", "male"),
    FEMALE("女", "female"),
}

enum class AgeFilter(
    val label: String,
    val min: Int?,
    val max: Int?,
) {
    ALL("全部年龄", null, null),
    AGE_0_18("0-18岁", 0, 18),
    AGE_19_40("19-40岁", 19, 40),
    AGE_41_60("41-60岁", 41, 60),
    AGE_60_PLUS("60岁以上", 61, null),
}

data class PatientsUiState(
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val search: String = "",
    val genderFilter: GenderFilter = GenderFilter.ALL,
    val ageFilter: AgeFilter = AgeFilter.ALL,
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

    fun updateGenderFilter(value: GenderFilter) {
        _state.update { it.copy(genderFilter = value) }
    }

    fun updateAgeFilter(value: AgeFilter) {
        _state.update { it.copy(ageFilter = value) }
    }

    fun refresh(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit = {},
    ) {
        loadInternal(
            session = session,
            repository = repository,
            page = 1,
            append = false,
            onSessionUpdated = onSessionUpdated,
            onSessionExpired = onSessionExpired,
        )
    }

    fun loadMore(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit = {},
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
            onSessionExpired = onSessionExpired,
        )
    }

    private fun loadInternal(
        session: UserSession,
        repository: PatientRepository,
        page: Int,
        append: Boolean,
        onSessionUpdated: (UserSession) -> Unit,
        onSessionExpired: (String) -> Unit,
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
            val gender = _state.value.genderFilter.queryValue
            val ageMin = _state.value.ageFilter.min
            val ageMax = _state.value.ageFilter.max
            when (
                val result = repository.loadPatients(
                    session = session,
                    page = page,
                    pageSize = 10,
                    search = search,
                    gender = gender,
                    ageMin = ageMin,
                    ageMax = ageMax,
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
                    if (result.notifySessionExpired(onSessionExpired)) {
                        _state.update {
                            it.copy(
                                loading = false,
                                loadingMore = false,
                                errorMessage = null,
                            )
                        }
                    } else {
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
}
