package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.DatePickerField
import com.xiehe.spine.ui.components.FilterSelector
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.PickerDialog
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PatientFormViewModel

private data class GenderOption(
    val label: String,
    val value: String,
)

@Composable
fun PatientFormScreen(
    vm: PatientFormViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()
    var genderPickerVisible by remember { mutableStateOf(false) }

    val options = listOf(
        GenderOption("男", "male"),
        GenderOption("女", "female"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(value = state.name, onValueChange = vm::updateName, placeholder = "请输入患者姓名")

            FilterSelector(
                text = options.firstOrNull { it.value == state.gender }?.label ?: "请选择患者性别",
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.PROFILE,
                onClick = { genderPickerVisible = true },
            )

            DatePickerField(
                value = state.birthDate,
                onValueChange = vm::updateBirthDate,
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(value = state.idCard, onValueChange = vm::updateIdCard, placeholder = "请输入18位身份证号")
            TextField(value = state.phone, onValueChange = vm::updatePhone, placeholder = "+8613800138000")
            TextField(value = state.email, onValueChange = vm::updateEmail, placeholder = "请输入邮箱(可选)")
            TextField(value = state.address, onValueChange = vm::updateAddress, placeholder = "请输入家庭地址")
            TextField(
                value = state.medicalHistory,
                onValueChange = vm::updateMedicalHistory,
                placeholder = "请输入病史备注",
                singleLine = false,
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }

            Button(
                text = if (state.loading) "提交中..." else "创建患者",
                onClick = {
                    vm.submit(
                        session = session,
                        repository = repository,
                        onSessionUpdated = onSessionUpdated,
                        onSuccess = onSubmitSuccess,
                    )
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.CHECK,
            )
        }

        if (state.loading) {
            LoadingOverlay(message = "...正在加载中")
        }
    }

    if (genderPickerVisible) {
        PickerDialog(
            title = "",
            onDismissRequest = { genderPickerVisible = false },
            showActionRow = false,
        ) { dismiss ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "请选择患者性别", style = SpineTheme.typography.title)
                options.forEach { option ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (state.gender == option.value) SpineTheme.colors.primaryMuted else SpineTheme.colors.surface,
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
                        Text(option.label)
                        if (state.gender == option.value) {
                            Text("✓", color = SpineTheme.colors.primary)
                        }
                    }
                }
            }
        }
    }
}
