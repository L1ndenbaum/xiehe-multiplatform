package com.xiehe.spine.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.form.picker.DatePickerField
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.patient.PatientEditViewModel

private data class EditGenderOption(
    val label: String,
    val value: String,
)

private enum class PatientEditPicker {
    GENDER,
    PHONE_PREFIX,
}

@Composable
fun PatientEditScreen(
    patientId: Int,
    vm: PatientEditViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()
    var picker by remember { mutableStateOf<PatientEditPicker?>(null) }

    val genderOptions = listOf(
        EditGenderOption("男", "male"),
        EditGenderOption("女", "female"),
    )
    val phonePrefixOptions = listOf("+86", "+852", "+853", "+886")

    LaunchedEffect(patientId, session.accessToken) {
        vm.load(
            patientId = patientId,
            session = session,
            repository = repository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextField(
                value = state.name,
                onValueChange = vm::updateName,
                placeholder = "请输入患者姓名",
                leadingGlyph = IconToken.PROFILE,
            )

            PickerField(
                text = genderOptions.firstOrNull { it.value == state.gender }?.label ?: "请选择患者性别",
                onClick = { picker = PatientEditPicker.GENDER },
                leadingGlyph = IconToken.PROFILE,
            )

            DatePickerField(
                value = state.birthDate,
                onValueChange = vm::updateBirthDate,
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = state.idCard,
                onValueChange = vm::updateIdCard,
                placeholder = "请输入18位身份证号",
                leadingGlyph = IconToken.LOCK,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PickerField(
                    text = state.phonePrefix,
                    onClick = { picker = PatientEditPicker.PHONE_PREFIX },
                    leadingGlyph = IconToken.MESSAGE,
                    modifier = Modifier.weight(0.36f),
                )
                TextField(
                    value = state.phoneLocalNumber,
                    onValueChange = vm::updatePhoneLocalNumber,
                    placeholder = if (state.phonePrefix == "+86") "请输入11位手机号" else "请输入号码",
                    leadingGlyph = IconToken.MESSAGE,
                    modifier = Modifier.weight(0.64f),
                )
            }

            TextField(
                value = state.email,
                onValueChange = vm::updateEmail,
                placeholder = "请输入邮箱(可选)",
                leadingGlyph = IconToken.MESSAGE,
            )

            TextField(
                value = state.address,
                onValueChange = vm::updateAddress,
                placeholder = "请输入家庭地址(可选)",
                leadingGlyph = IconToken.SETTINGS,
            )

            TextField(
                value = state.emergencyContactName,
                onValueChange = vm::updateEmergencyContactName,
                placeholder = "请输入紧急联系人(可选)",
                leadingGlyph = IconToken.PROFILE,
            )

            TextField(
                value = state.emergencyContactPhone,
                onValueChange = vm::updateEmergencyContactPhone,
                placeholder = "请输入紧急联系电话(可选)",
                leadingGlyph = IconToken.MESSAGE,
            )

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
            state.successMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.success))
            }

            Button(
                text = if (state.submitting) "保存中..." else "保存修改",
                onClick = {
                    vm.submit(
                        session = session,
                        repository = repository,
                        onSessionUpdated = onSessionUpdated,
                        onSuccess = onSubmitSuccess,
                    )
                },
                enabled = !state.loading && !state.submitting,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.CHECK,
            )
        }

        if (state.loading || state.submitting) {
            LoadingOverlay(message = "...正在加载中")
        }
    }

    when (picker) {
        PatientEditPicker.GENDER -> {
            PickerDialog(
                title = "",
                onDismissRequest = { picker = null },
                showActionRow = false,
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "请选择患者性别", style = SpineTheme.typography.title)
                    genderOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (state.gender == option.value) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                                    shape = RoundedCornerShape(SpineTheme.radius.md),
                                )
                                .clickable {
                                    vm.updateGender(option.value)
                                    dismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(option.label, color = if (state.gender == option.value) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary)
                            if (state.gender == option.value) {
                                Text("✓", color = SpineTheme.colors.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        PatientEditPicker.PHONE_PREFIX -> {
            PickerDialog(
                title = "",
                onDismissRequest = { picker = null },
                showActionRow = false,
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "请选择电话区号", style = SpineTheme.typography.title)
                    phonePrefixOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (state.phonePrefix == option) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                                    shape = RoundedCornerShape(SpineTheme.radius.md),
                                )
                                .clickable {
                                    vm.updatePhonePrefix(option)
                                    dismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(option, color = if (state.phonePrefix == option) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary)
                            if (state.phonePrefix == option) {
                                Text("✓", color = SpineTheme.colors.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        null -> Unit
    }
}

@Composable
private fun PickerField(
    text: String,
    onClick: () -> Unit,
    leadingGlyph: IconToken,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = text,
        onValueChange = {},
        placeholder = text,
        modifier = modifier.clickable(onClick = onClick),
        readOnly = true,
        leadingGlyph = leadingGlyph,
        trailingGlyph = IconToken.CHEVRON_DOWN,
        onTrailingClick = onClick,
    )
}
