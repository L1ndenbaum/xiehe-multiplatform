package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.CreatePatientRequest
import com.xiehe.spine.data.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
