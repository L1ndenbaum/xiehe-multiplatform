package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PersonalInfoViewModel

@Composable
fun PersonalInfoScreen(
    vm: PersonalInfoViewModel,
    session: UserSession,
    authRepository: AuthRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(session.accessToken) {
        vm.seedFromSession(session)
        vm.load(
            session = session,
            repository = authRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ReadonlyField(
            label = "用户名",
            value = state.username,
            hint = "用户名不可修改",
        )
        ReadonlyField(
            label = "邮箱地址",
            value = state.email,
            hint = "邮箱不可修改",
        )

        EditableField(
            label = "真实姓名",
            value = state.realName,
            placeholder = "请输入真实姓名",
            onValueChange = vm::updateRealName,
        )
        EditableField(
            label = "手机号",
            value = state.phone,
            placeholder = "请输入手机号",
            onValueChange = vm::updatePhone,
        )
        EditableField(
            label = "职位",
            value = state.position,
            placeholder = "请输入职位",
            onValueChange = vm::updatePosition,
        )
        EditableField(
            label = "职称",
            value = state.title,
            placeholder = "请输入职称",
            onValueChange = vm::updateTitle,
        )

        state.errorMessage?.let {
            Text(
                text = it,
                style = SpineTheme.typography.subhead,
                color = SpineTheme.colors.error,
            )
        }
        state.successMessage?.let {
            Text(
                text = it,
                style = SpineTheme.typography.subhead,
                color = SpineTheme.colors.primary,
            )
        }

        Button(
            text = if (state.saving) "保存中..." else "保存更改",
            onClick = {
                vm.save(
                    session = session,
                    repository = authRepository,
                    onSessionUpdated = onSessionUpdated,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.saving,
            leadingGlyph = IconToken.CHECK,
        )
    }

    if (state.loading || state.saving) {
        LoadingOverlay(message = "...正在加载中")
    }
}

@Composable
private fun ReadonlyField(
    label: String,
    value: String,
    hint: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = SpineTheme.typography.subhead,
            color = SpineTheme.colors.textSecondary,
        )
        TextField(
            value = value,
            onValueChange = {},
            placeholder = "",
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = hint,
            style = SpineTheme.typography.caption,
            color = SpineTheme.colors.textTertiary,
        )
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = SpineTheme.typography.subhead,
            color = SpineTheme.colors.textSecondary,
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
