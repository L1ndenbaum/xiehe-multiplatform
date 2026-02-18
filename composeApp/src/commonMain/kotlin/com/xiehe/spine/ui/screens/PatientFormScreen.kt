package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineSelectablePill
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PatientFormViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .verticalScroll(scroll)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpineTextField(value = state.name, onValueChange = vm::updateName, placeholder = "患者姓名")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SpineSelectablePill(text = "男", selected = state.gender == "男", onClick = { vm.updateGender("男") })
            SpineSelectablePill(text = "女", selected = state.gender == "女", onClick = { vm.updateGender("女") })
        }
        SpineTextField(value = state.birthDate, onValueChange = vm::updateBirthDate, placeholder = "出生日期(yyyy-MM-dd)")
        SpineTextField(value = state.phone, onValueChange = vm::updatePhone, placeholder = "手机号")
        SpineTextField(value = state.idCard, onValueChange = vm::updateIdCard, placeholder = "身份证号")
        SpineTextField(value = state.address, onValueChange = vm::updateAddress, placeholder = "联系地址")
        state.errorMessage?.let {
            SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
        }
        SpineButton(
            text = if (state.loading) "提交中..." else "保存患者",
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
            leadingGlyph = SpineGlyph.CHECK,
        )
    }
}
