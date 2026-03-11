package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.theme.SpineTheme

private enum class PasswordStep {
    VERIFY,
    RESET,
    DONE,
}

@Composable
fun ChangePasswordScreen() {
    var step by remember { mutableStateOf(PasswordStep.VERIFY) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StepIndicator(step = step)

            when (step) {
                PasswordStep.VERIFY -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("验证当前密码", style = SpineTheme.typography.title)
                        TextField(
                            value = currentPassword,
                            onValueChange = {
                                currentPassword = it
                                errorMessage = null
                            },
                            placeholder = "请输入当前密码",
                            password = !showCurrent,
                            leadingGlyph = IconToken.LOCK,
                            trailingGlyph = if (showCurrent) IconToken.EYE_OFF else IconToken.EYE,
                            onTrailingClick = { showCurrent = !showCurrent },
                        )
                        errorMessage?.let {
                            Text(it, color = SpineTheme.colors.error)
                        }
                        Button(
                            text = "下一步",
                            onClick = {
                                if (currentPassword.length < 6) {
                                    errorMessage = "请输入正确的当前密码"
                                } else {
                                    errorMessage = null
                                    step = PasswordStep.RESET
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                PasswordStep.RESET -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("设置新密码", style = SpineTheme.typography.title)
                        TextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMessage = null
                            },
                            placeholder = "至少8位，建议包含字母和数字",
                            password = !showNew,
                            leadingGlyph = IconToken.LOCK,
                            trailingGlyph = if (showNew) IconToken.EYE_OFF else IconToken.EYE,
                            onTrailingClick = { showNew = !showNew },
                        )
                        TextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            placeholder = "再次输入新密码",
                            password = !showConfirm,
                            leadingGlyph = IconToken.LOCK,
                            trailingGlyph = if (showConfirm) IconToken.EYE_OFF else IconToken.EYE,
                            onTrailingClick = { showConfirm = !showConfirm },
                        )
                        errorMessage?.let {
                            Text(it, color = SpineTheme.colors.error)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                text = "上一步",
                                onClick = { step = PasswordStep.VERIFY },
                                modifier = Modifier.weight(1f),
                                customContainerColor = SpineTheme.colors.surfaceMuted,
                                customContentColor = SpineTheme.colors.textPrimary,
                            )
                            Button(
                                text = "确认修改",
                                onClick = {
                                    when {
                                        newPassword.length < 8 -> errorMessage = "密码至少8位"
                                        newPassword == currentPassword -> errorMessage = "新密码不能与当前密码相同"
                                        newPassword != confirmPassword -> errorMessage = "两次密码不一致"
                                        else -> {
                                            errorMessage = null
                                            step = PasswordStep.DONE
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                PasswordStep.DONE -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(64.dp)
                                    .fillMaxWidth(0.25f)
                                    .background(
                                        SpineTheme.colors.success.copy(alpha = 0.2f),
                                        RoundedCornerShape(SpineTheme.radius.full),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("✓", style = SpineTheme.typography.display, color = SpineTheme.colors.success)
                            }
                            Text("密码修改成功", style = SpineTheme.typography.title)
                            Text("本页面仅用于本地演示，不调用后端改密接口", color = SpineTheme.colors.textSecondary)
                            Button(
                                text = "再次修改",
                                onClick = {
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    step = PasswordStep.VERIFY
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: PasswordStep) {
    val colors = SpineTheme.colors
    val activeIndex = when (step) {
        PasswordStep.VERIFY -> 1
        PasswordStep.RESET -> 2
        PasswordStep.DONE -> 3
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepNode("验证身份", activeIndex >= 1, activeIndex > 1)
            StepNode("设置新密码", activeIndex >= 2, activeIndex > 2)
            StepNode("完成", activeIndex >= 3, false)
        }
    }
}

@Composable
private fun StepNode(
    label: String,
    active: Boolean,
    done: Boolean,
) {
    val colors = SpineTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (active) colors.primary else colors.surfaceMuted,
                    RoundedCornerShape(SpineTheme.radius.full),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (done) "✓" else label.first().toString(),
                color = if (active) colors.onPrimary else colors.textSecondary,
                style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        Text(
            text = label,
            style = SpineTheme.typography.caption,
            color = if (active) colors.primary else colors.textSecondary,
        )
    }
}
