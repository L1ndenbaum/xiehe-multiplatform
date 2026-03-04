package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.AppIcon
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(SpineTheme.colors.primaryMuted, RoundedCornerShape(SpineTheme.radius.lg)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(glyph = IconToken.PROFILE, tint = SpineTheme.colors.primary, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = state.realName.ifBlank { state.username.ifBlank { session.username } },
                            style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = state.position.ifBlank { "医生" },
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                ReadonlyField(
                    label = "用户名",
                    value = state.username,
                    hint = "用户名不可修改",
                    icon = IconToken.PROFILE,
                )
                ReadonlyField(
                    label = "邮箱地址",
                    value = state.email,
                    hint = "邮箱不可修改",
                    icon = IconToken.MESSAGE,
                )
                EditableField(
                    label = "真实姓名",
                    value = state.realName,
                    placeholder = "请输入真实姓名",
                    icon = IconToken.PROFILE,
                    onValueChange = vm::updateRealName,
                )
                EditableField(
                    label = "手机号",
                    value = state.phone,
                    placeholder = "请输入手机号",
                    icon = IconToken.MESSAGE,
                    onValueChange = vm::updatePhone,
                )
                EditableField(
                    label = "职位",
                    value = state.position,
                    placeholder = "请输入职位",
                    icon = IconToken.PATIENTS,
                    onValueChange = vm::updatePosition,
                )
                EditableField(
                    label = "职称",
                    value = state.title,
                    placeholder = "请输入职称",
                    icon = IconToken.CHECK,
                    onValueChange = vm::updateTitle,
                )
            }

            state.errorMessage?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it, color = SpineTheme.colors.error)
                }
            }
            state.successMessage?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it, color = SpineTheme.colors.success)
                }
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
}

@Composable
private fun ReadonlyField(
    label: String,
    value: String,
    hint: String,
    icon: IconToken,
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
            leadingGlyph = icon,
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
    icon: IconToken,
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
            leadingGlyph = icon,
        )
    }
}
