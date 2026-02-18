package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLogin: () -> Unit,
    onHealthCheck: () -> Unit,
    showNetworkDiagnostics: Boolean,
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing
    val colors = SpineTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.x2l),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            SpineText(text = "协和医疗", style = SpineTheme.typography.display)
            SpineText(text = "登录医疗影像诊断系统", style = SpineTheme.typography.subhead)
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.base)) {
            SpineTextField(
                value = state.username,
                onValueChange = vm::updateUsername,
                placeholder = "用户名",
                modifier = Modifier.fillMaxWidth(),
            )
            SpineTextField(
                value = state.password,
                onValueChange = vm::updatePassword,
                placeholder = "密码",
                password = true,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + slideInVertically { -it / 3 },
                exit = fadeOut() + slideOutVertically { -it / 3 },
            ) {
                SpineText(
                    text = state.errorMessage ?: "",
                    style = SpineTheme.typography.subhead.copy(color = colors.error),
                )
            }
            state.errorDetails?.let {
                SpineText(
                    text = it,
                    style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                )
            }
            SpineButton(
                text = if (state.loading) "登录中..." else "登录",
                onClick = onLogin,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = SpineGlyph.PROFILE,
            )
            if (showNetworkDiagnostics) {
                SpineButton(
                    text = if (state.healthChecking) "检测中..." else "连接自检(/health)",
                    onClick = onHealthCheck,
                    enabled = !state.healthChecking,
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = SpineGlyph.BELL,
                )
                state.healthStatus?.let {
                    SpineText(
                        text = it,
                        style = SpineTheme.typography.subhead.copy(
                            color = if (it.startsWith("后端连通")) colors.success else colors.warning,
                        ),
                    )
                }
                state.healthDetails?.let {
                    SpineText(
                        text = it,
                        style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                    )
                }
            }
        }
    }
}
