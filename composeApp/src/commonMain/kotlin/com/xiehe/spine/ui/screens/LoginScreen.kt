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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.AppIcon
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.Card
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
    val colors = SpineTheme.colors
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.primaryMuted,
                        colors.background,
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 16.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.09f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 12.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.08f)),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(colors.primary, colors.primary.copy(alpha = 0.82f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        glyph = IconToken.IMAGE,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(text = "协和医疗", style = SpineTheme.typography.display)
                Text(
                    text = "脊柱医疗影像管理系统",
                    style = SpineTheme.typography.subhead,
                    color = colors.textSecondary,
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = "欢迎登录", style = SpineTheme.typography.title)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = state.username,
                        onValueChange = vm::updateUsername,
                        placeholder = "请输入账号",
                        modifier = Modifier.fillMaxWidth(),
                        leadingGlyph = IconToken.PROFILE,
                    )
                    TextField(
                        value = state.password,
                        onValueChange = vm::updatePassword,
                        placeholder = "请输入密码",
                        password = !passwordVisible,
                        modifier = Modifier.fillMaxWidth(),
                        leadingGlyph = IconToken.LOCK,
                        trailingGlyph = if (passwordVisible) IconToken.EYE_OFF else IconToken.EYE,
                        onTrailingClick = { passwordVisible = !passwordVisible },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (rememberPassword) colors.primary else colors.surfaceMuted)
                                .clickable { rememberPassword = !rememberPassword },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (rememberPassword) {
                                Text(text = "✓", style = SpineTheme.typography.caption, color = colors.onPrimary)
                            }
                        }
                        Text(text = "记住密码", style = SpineTheme.typography.subhead, color = colors.textSecondary)
                    }
                    Text(
                        text = "忘记密码?",
                        style = SpineTheme.typography.subhead.copy(color = colors.primary),
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
                        style = SpineTheme.typography.caption,
                        color = colors.textSecondary,
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
                    Text(text = "还没有账户?", style = SpineTheme.typography.subhead, color = colors.textSecondary)
                    Text(
                        text = " 立即注册",
                        style = SpineTheme.typography.subhead.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier.clickable(onClick = onOpenRegister),
                    )
                }
            }

            if (showNetworkDiagnostics) {
                Card(modifier = Modifier.fillMaxWidth()) {
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
                            style = SpineTheme.typography.caption,
                            color = colors.textSecondary,
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
