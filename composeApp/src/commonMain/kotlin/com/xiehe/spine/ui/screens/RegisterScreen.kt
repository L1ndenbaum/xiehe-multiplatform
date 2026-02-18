package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.data.AuthRepository
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    vm: RegisterViewModel,
    authRepository: AuthRepository,
    onBackToLogin: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val colors = SpineTheme.colors
    val spacing = SpineTheme.spacing
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.base),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpineText(
                text = "返回",
                style = SpineTheme.typography.subhead.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable(onClick = onBackToLogin),
            )
            SpineText(text = "账号注册", style = SpineTheme.typography.title)
        }

        SpineTextField(
            value = state.username,
            onValueChange = vm::updateUsername,
            placeholder = "用户名(至少3位)",
            modifier = Modifier.fillMaxWidth(),
        )
        SpineTextField(
            value = state.fullName,
            onValueChange = vm::updateFullName,
            placeholder = "姓名(至少2位)",
            modifier = Modifier.fillMaxWidth(),
        )
        SpineTextField(
            value = state.email,
            onValueChange = vm::updateEmail,
            placeholder = "邮箱",
            modifier = Modifier.fillMaxWidth(),
        )
        SpineTextField(
            value = state.phone,
            onValueChange = vm::updatePhone,
            placeholder = "手机号(可选,+86开头)",
            modifier = Modifier.fillMaxWidth(),
            leadingGlyph = SpineGlyph.PROFILE,
        )
        SpineTextField(
            value = state.password,
            onValueChange = vm::updatePassword,
            placeholder = "密码(至少6位)",
            password = !passwordVisible,
            modifier = Modifier.fillMaxWidth(),
            trailingGlyph = if (passwordVisible) SpineGlyph.EYE_OFF else SpineGlyph.EYE,
            onTrailingClick = { passwordVisible = !passwordVisible },
        )
        SpineTextField(
            value = state.confirmPassword,
            onValueChange = vm::updateConfirmPassword,
            placeholder = "确认密码",
            password = !confirmVisible,
            modifier = Modifier.fillMaxWidth(),
            trailingGlyph = if (confirmVisible) SpineGlyph.EYE_OFF else SpineGlyph.EYE,
            onTrailingClick = { confirmVisible = !confirmVisible },
        )

        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
        ) {
            SpineText(
                text = state.errorMessage ?: "",
                style = SpineTheme.typography.subhead.copy(color = colors.error),
            )
        }

        state.successMessage?.let {
            SpineText(
                text = it,
                style = SpineTheme.typography.subhead.copy(color = colors.success),
            )
        }

        SpineButton(
            text = if (state.loading) "注册中..." else "立即注册",
            onClick = {
                vm.submit(
                    authRepository = authRepository,
                    onSuccess = onBackToLogin,
                )
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
