package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.AppIcon
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.IconBadge
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.painterResource
import spine.composeapp.generated.resources.Res
import spine.composeapp.generated.resources.login_logo

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
                        Color(0xFFF5F3FF),
                        Color(0xFFFAF5FF),
                        Color(0xFFFDF2F8),
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 0.dp, top = 0.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 0.dp, bottom = 0.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(-3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.login_logo),
                        contentDescription = "login_logo",
                        modifier = Modifier
                            .size(56.dp)
                            .rotate(3f),
                    )
                }
                Text(
                    text = "协和医疗",
                    style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                )
                Text(
                    text = "脊柱医疗影像管理系统",
                    style = SpineTheme.typography.body,
                    color = colors.textSecondary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "欢迎登录",
                    style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )

                LoginInputField(
                    label = "账号",
                    value = state.username,
                    onValueChange = vm::updateUsername,
                    placeholder = "请输入账号",
                    leadingGlyph = IconToken.USER,
                    leadingColors = listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6)),
                )
                LoginInputField(
                    label = "密码",
                    value = state.password,
                    onValueChange = vm::updatePassword,
                    placeholder = "请输入密码",
                    leadingGlyph = IconToken.LOCK,
                    leadingColors = listOf(Color(0xFFC084FC), Color(0xFFA855F7)),
                    password = !passwordVisible,
                    trailingGlyph = if (passwordVisible) IconToken.EYE_OFF else IconToken.EYE,
                    onTrailingClick = { passwordVisible = !passwordVisible },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (rememberPassword) {
                                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF9333EA)))
                                    } else {
                                        Brush.linearGradient(listOf(colors.surfaceMuted, colors.surfaceMuted))
                                    },
                                )
                                .clickable { rememberPassword = !rememberPassword },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (rememberPassword) {
                                AppIcon(
                                    glyph = IconToken.CHECK,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                        Text(text = "记住密码", style = SpineTheme.typography.subhead, color = colors.textSecondary)
                    }
                    Text(
                        text = "忘记密码?",
                        style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary,
                    )
                }

                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn() + slideInVertically { -it / 3 },
                    exit = fadeOut() + slideOutVertically { -it / 3 },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = state.errorMessage ?: "",
                            style = SpineTheme.typography.subhead.copy(color = colors.error),
                        )
                        state.errorDetails?.let {
                            Text(
                                text = it,
                                style = SpineTheme.typography.caption,
                                color = colors.textSecondary,
                            )
                        }
                    }
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
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.clickable(onClick = onOpenRegister),
                    )
                }
            }

            if (showNetworkDiagnostics) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.78f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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

            Text(
                text = "© 2024 协和医疗 版权所有",
                style = SpineTheme.typography.caption,
                color = Color(0xFFA1A1AA),
            )
        }

        if (state.loading || state.healthChecking) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

@Composable
private fun LoginInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingGlyph: IconToken,
    leadingColors: List<Color>,
    password: Boolean = false,
    trailingGlyph: IconToken? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    val colors = SpineTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF475569),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                glyph = leadingGlyph,
                size = 24.dp,
                iconSize = 14.dp,
                cornerRadius = 8.dp,
                colors = leadingColors,
                shadowColor = leadingColors.last().copy(alpha = 0.18f),
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = SpineTheme.typography.body,
                        color = Color(0xFF94A3B8),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = SpineTheme.typography.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.primary),
                    visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
                )
            }
            if (trailingGlyph != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable(enabled = onTrailingClick != null) {
                            onTrailingClick?.invoke()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(glyph = trailingGlyph, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}