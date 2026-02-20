package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLogin: () -> Unit,
    onHealthCheck: () -> Unit,
    showNetworkDiagnostics: Boolean,
    onOpenRegister: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing
    val colors = SpineTheme.colors
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.x2l),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Text(text = "协和医疗", style = SpineTheme.typography.display)
                Text(text = "登录医疗影像诊断系统", style = SpineTheme.typography.subhead)
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.base)) {
                Text(text = "用户名或邮箱", style = SpineTheme.typography.subhead)
                TextField(
                    value = state.username,
                    onValueChange = vm::updateUsername,
                    placeholder = "请输入用户名或邮箱",
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(text = "密码", style = SpineTheme.typography.subhead)
                TextField(
                    value = state.password,
                    onValueChange = vm::updatePassword,
                    placeholder = "请输入密码",
                    password = !passwordVisible,
                    modifier = Modifier.fillMaxWidth(),
                    trailingGlyph = if (passwordVisible) IconToken.EYE_OFF else IconToken.EYE,
                    onTrailingClick = { passwordVisible = !passwordVisible },
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "忘记密码?",
                        style = SpineTheme.typography.subhead.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                    )
                }

                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn() + slideInVertically { -it / 3 },
                    exit = fadeOut() + slideOutVertically { -it / 3 },
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = SpineTheme.typography.subhead.copy(color = colors.error),
                    )
                }

                state.errorDetails?.let {
                    Text(
                        text = it,
                        style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                    )
                }

                Button(
                    text = if (state.loading) "登录中..." else "登录",
                    onClick = onLogin,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "还没有账号?", style = SpineTheme.typography.subhead)
                    Text(
                        text = " 立即注册",
                        style = SpineTheme.typography.subhead.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable(onClick = onOpenRegister),
                    )
                }

                if (showNetworkDiagnostics) {
                    Button(
                        text = if (state.healthChecking) "检测中..." else "连接自检(/health)",
                        onClick = onHealthCheck,
                        enabled = !state.healthChecking,
                        modifier = Modifier.fillMaxWidth(),
                        leadingGlyph = IconToken.BELL,
                    )
                    state.healthStatus?.let {
                        Text(
                            text = it,
                            style = SpineTheme.typography.subhead.copy(
                                color = if (it.startsWith("后端连通")) colors.success else colors.warning,
                            ),
                        )
                    }
                    state.healthDetails?.let {
                        Text(
                            text = it,
                            style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                        )
                    }
                }
            }
        }

        if (state.loading || state.healthChecking) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}
