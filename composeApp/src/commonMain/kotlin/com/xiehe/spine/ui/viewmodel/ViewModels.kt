package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.ThemePreferenceRepository
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.data.CreatePatientRequest
import com.xiehe.spine.data.DashboardOverview
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.PatientDetail
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.data.PatientSummary
import com.xiehe.spine.ui.theme.ThemeBrand
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.theme.ThemePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class BaseViewModel {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun updateUsername(value: String) {
        _state.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun submit(
        authRepository: AuthRepository,
        onSuccess: (UserSession) -> Unit,
    ) {
        val current = _state.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(errorMessage = "请输入用户名和密码") }
            return
        }
        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = authRepository.login(current.username.trim(), current.password)) {
                is AppResult.Success -> {
                    _state.update { it.copy(loading = false) }
                    onSuccess(result.data)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(loading = false, errorMessage = result.message) }
                }
            }
        }
    }
}

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

data class PatientFormUiState(
    val patientId: String = generatedPatientId(),
    val name: String = "",
    val gender: String = "男",
    val birthDate: String = "1990-01-01",
    val phone: String = "",
    val idCard: String = "",
    val address: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

class PatientFormViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(PatientFormUiState())
    val state: StateFlow<PatientFormUiState> = _state.asStateFlow()

    fun updateName(value: String) = _state.update { it.copy(name = value) }
    fun updateGender(value: String) = _state.update { it.copy(gender = value) }
    fun updateBirthDate(value: String) = _state.update { it.copy(birthDate = value) }
    fun updatePhone(value: String) = _state.update { it.copy(phone = value) }
    fun updateIdCard(value: String) = _state.update { it.copy(idCard = value) }
    fun updateAddress(value: String) = _state.update { it.copy(address = value) }

    fun submit(
        session: UserSession,
        repository: PatientRepository,
        onSessionUpdated: (UserSession) -> Unit,
        onSuccess: () -> Unit,
    ) {
        val form = _state.value
        if (form.name.isBlank() || form.phone.isBlank()) {
            _state.update { it.copy(errorMessage = "患者姓名与手机号为必填") }
            return
        }

        val request = CreatePatientRequest(
            patientId = form.patientId,
            name = form.name,
            gender = form.gender,
            birthDate = form.birthDate,
            phone = form.phone,
            idCard = form.idCard,
            address = form.address,
        )

        scope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.createPatient(session, request)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.value = PatientFormUiState()
                    onSuccess()
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(loading = false, errorMessage = result.message) }
                }
            }
        }
    }
}

class AppearanceViewModel(
    private val repository: ThemePreferenceRepository,
) {
    val state: StateFlow<ThemePreference> = repository.preference

    fun updateBrand(brand: ThemeBrand) {
        repository.updateBrand(brand)
    }

    fun updateMode(mode: ThemeMode) {
        repository.updateMode(mode)
    }
}

private fun generatedPatientId(): String {
    return "P${kotlin.random.Random.nextInt(10000000, 99999999)}"
}
