package com.xiehe.spine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.theme.SpineTheme

private enum class PasswordStep {
    VERIFY,
    RESET,
    DONE,
}

private data class PasswordStrength(
    val label: String,
    val color: Color,
    val score: Int,
)

private val PasswordPageBackground = Color(0xFFF1F5F9)
private val PasswordCardBorder = Color(0xFFE2E8F0)
private val PasswordCardShadow = Color(0x120F172A)
private val PasswordMutedText = Color(0xFF94A3B8)
private val PasswordBodyText = Color(0xFF64748B)
private val PasswordTitleText = Color(0xFF0F172A)
private val PasswordPurple = Color(0xFF8B5CF6)
private val PasswordPurpleDark = Color(0xFF7C3AED)
private val PasswordSuccess = Color(0xFF10B981)
private val PasswordSuccessLight = Color(0xFF34D399)
private val PasswordWarning = Color(0xFFF59E0B)
private val PasswordNeutralLine = Color(0xFFE2E8F0)

@Composable
fun ChangePasswordScreen(
    onFinished: () -> Unit = {},
) {
    var step by remember { mutableStateOf(PasswordStep.VERIFY) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val strength = remember(newPassword) { evaluatePasswordStrength(newPassword) }
    val matchesConfirm = confirmPassword.isNotBlank() && confirmPassword == newPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PasswordPageBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PasswordStepCard(step = step)

            when (step) {
                PasswordStep.VERIFY -> {
                    PasswordVerifyCard(
                        currentPassword = currentPassword,
                        showCurrent = showCurrent,
                        errorMessage = errorMessage,
                        onPasswordChange = {
                            currentPassword = it
                            errorMessage = null
                        },
                        onToggleVisibility = { showCurrent = !showCurrent },
                        onNext = {
                            if (currentPassword.length < 6) {
                                errorMessage = "请输入正确的当前密码"
                            } else {
                                errorMessage = null
                                step = PasswordStep.RESET
                            }
                        },
                    )
                    PasswordSafetyTipCard()
                }

                PasswordStep.RESET -> {
                    PasswordResetCard(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        showNew = showNew,
                        showConfirm = showConfirm,
                        strength = strength,
                        matchesConfirm = matchesConfirm,
                        errorMessage = errorMessage,
                        onNewPasswordChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        onToggleNewVisibility = { showNew = !showNew },
                        onToggleConfirmVisibility = { showConfirm = !showConfirm },
                        onBack = {
                            errorMessage = null
                            step = PasswordStep.VERIFY
                        },
                        onConfirm = {
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
                    )
                    PasswordSafetyTipCard()
                }

                PasswordStep.DONE -> {
                    PasswordDoneCard(
                        onBackToProfile = onFinished,
                        onReset = {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            showCurrent = false
                            showNew = false
                            showConfirm = false
                            errorMessage = null
                            step = PasswordStep.VERIFY
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PasswordStepCard(step: PasswordStep) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .shadow(12.dp, shape, ambientColor = PasswordCardShadow, spotColor = PasswordCardShadow)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, PasswordCardBorder, shape)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        PasswordStepNode(
            label = "验证身份",
            index = 1,
            state = when (step) {
                PasswordStep.VERIFY -> StepNodeState.ACTIVE
                PasswordStep.RESET, PasswordStep.DONE -> StepNodeState.DONE
            },
            modifier = Modifier.weight(1f),
        )
        PasswordStepConnector(completed = step == PasswordStep.RESET || step == PasswordStep.DONE)
        PasswordStepNode(
            label = "设置新密码",
            index = 2,
            state = when (step) {
                PasswordStep.VERIFY -> StepNodeState.INACTIVE
                PasswordStep.RESET -> StepNodeState.ACTIVE
                PasswordStep.DONE -> StepNodeState.DONE
            },
            modifier = Modifier.weight(1f),
        )
        PasswordStepConnector(completed = step == PasswordStep.DONE)
        PasswordStepNode(
            label = "完成",
            index = 3,
            state = if (step == PasswordStep.DONE) StepNodeState.DONE else StepNodeState.INACTIVE,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class StepNodeState {
    ACTIVE,
    DONE,
    INACTIVE,
}

@Composable
private fun PasswordStepNode(
    label: String,
    index: Int,
    state: StepNodeState,
    modifier: Modifier = Modifier,
) {
    val container = when (state) {
        StepNodeState.ACTIVE -> Brush.linearGradient(listOf(PasswordPurple, PasswordPurpleDark))
        StepNodeState.DONE -> Brush.linearGradient(listOf(PasswordSuccessLight, PasswordSuccess))
        StepNodeState.INACTIVE -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9)))
    }
    val textColor = if (state == StepNodeState.INACTIVE) PasswordMutedText else Color.White
    val labelColor = when (state) {
        StepNodeState.ACTIVE -> PasswordPurple
        StepNodeState.DONE -> PasswordMutedText
        StepNodeState.INACTIVE -> PasswordMutedText
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (state == StepNodeState.DONE) "✓" else index.toString(),
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = textColor,
            )
        }
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = if (state == StepNodeState.ACTIVE) FontWeight.SemiBold else FontWeight.Medium),
            color = labelColor,
        )
    }
}

@Composable
private fun PasswordStepConnector(completed: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = 15.dp)
            .width(44.dp)
            .height(2.dp)
            .background(if (completed) PasswordSuccessLight else PasswordNeutralLine, RoundedCornerShape(999.dp)),
    )
}

@Composable
private fun PasswordVerifyCard(
    currentPassword: String,
    showCurrent: Boolean,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onNext: () -> Unit,
) {
    PasswordSurfaceCard {
        PasswordCardTitle(title = "验证当前密码")
        PasswordInputBlock(
            label = "当前密码",
            value = currentPassword,
            placeholder = "请输入当前密码",
            visible = showCurrent,
            onValueChange = onPasswordChange,
            onToggleVisibility = onToggleVisibility,
        )
        errorMessage?.let {
            PasswordInlineMessage(message = it, color = Color(0xFFDC2626), background = Color(0xFFFEF2F2))
        }
        Button(
            text = "下一步",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}

@Composable
private fun PasswordResetCard(
    newPassword: String,
    confirmPassword: String,
    showNew: Boolean,
    showConfirm: Boolean,
    strength: PasswordStrength,
    matchesConfirm: Boolean,
    errorMessage: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    PasswordSurfaceCard {
        PasswordCardTitle(title = "设置新密码")
        PasswordInputBlock(
            label = "新密码",
            value = newPassword,
            placeholder = "请输入新密码",
            visible = showNew,
            onValueChange = onNewPasswordChange,
            onToggleVisibility = onToggleNewVisibility,
        )
        PasswordStrengthBar(strength = strength)
        PasswordInputBlock(
            label = "确认新密码",
            value = confirmPassword,
            placeholder = "请再次输入新密码",
            visible = showConfirm,
            onValueChange = onConfirmPasswordChange,
            onToggleVisibility = onToggleConfirmVisibility,
        )
        if (matchesConfirm) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF16A34A),
                    )
                }
                Text(
                    text = "两次密码一致",
                    style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF16A34A),
                )
            }
        }
        PasswordRequirementCard(password = newPassword)
        errorMessage?.let {
            PasswordInlineMessage(message = it, color = Color(0xFFDC2626), background = Color(0xFFFEF2F2))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PasswordSecondaryButton(
                text = "上一步",
                onClick = onBack,
                modifier = Modifier.weight(1f),
            )
            Button(
                text = "确认修改",
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(52.dp),
            )
        }
    }
}

@Composable
private fun PasswordDoneCard(
    onBackToProfile: () -> Unit,
    onReset: () -> Unit,
) {
    PasswordSurfaceCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(18.dp, CircleShape, ambientColor = Color(0x3310B981), spotColor = Color(0x3310B981))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PasswordSuccessLight, PasswordSuccess))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Text(
                text = "密码修改成功",
                style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B),
            )
            Text(
                text = "您的密码已更新，请妥善保管新密码",
                style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
                color = PasswordBodyText,
            )
            Button(
                text = "返回个人中心",
                onClick = onBackToProfile,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
            PasswordSecondaryButton(
                text = "再次修改",
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PasswordSurfaceCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .shadow(16.dp, shape, ambientColor = PasswordCardShadow, spotColor = PasswordCardShadow)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, PasswordCardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun PasswordCardTitle(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFA855F7), PasswordPurpleDark))),
            )
            Text(
                text = title,
                style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                color = PasswordTitleText,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEEF2F7)),
        )
    }
}

@Composable
private fun PasswordInputBlock(
    label: String,
    value: String,
    placeholder: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = PasswordBodyText,
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            password = !visible,
            leadingGlyph = com.xiehe.spine.ui.components.icon.shared.IconToken.LOCK,
            trailingGlyph = if (visible) com.xiehe.spine.ui.components.icon.shared.IconToken.EYE_OFF else com.xiehe.spine.ui.components.icon.shared.IconToken.EYE,
            onTrailingClick = onToggleVisibility,
        )
    }
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (index < strength.score) strength.color else Color(0xFFE5E7EB)),
                )
            }
        }
        Text(
            text = "密码强度: ${strength.label}",
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = strength.color,
        )
    }
}

@Composable
private fun PasswordRequirementCard(password: String) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF5F3FF))
            .border(1.dp, Color(0xFFE9D5FF), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "密码要求",
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Bold),
            color = PasswordPurpleDark,
        )
        PasswordRequirementRow(label = "至少8个字符", met = password.length >= 8)
        PasswordRequirementRow(label = "包含大写字母", met = password.any { it.isUpperCase() })
        PasswordRequirementRow(label = "包含数字", met = password.any { it.isDigit() })
        PasswordRequirementRow(label = "包含特殊字符（推荐）", met = password.any { !it.isLetterOrDigit() })
    }
}

@Composable
private fun PasswordRequirementRow(label: String, met: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (met) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF16A34A),
                )
            }
        } else {
            Text(
                text = "-",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFCBD5E1),
            )
        }
        Text(
            text = label,
            style = SpineTheme.typography.subhead.copy(fontWeight = if (met) FontWeight.SemiBold else FontWeight.Medium),
            color = if (met) Color(0xFF16A34A) else PasswordMutedText,
        )
    }
}

@Composable
private fun PasswordSafetyTipCard() {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .clip(shape)
            .background(Color(0xFFFFF7ED))
            .border(1.dp, Color(0xFFFED7AA), shape)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "!",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF59E0B),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "安全提示",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFC2410C),
            )
            Text(
                text = "请勿使用生日、手机号等易猜测的密码，建议定期更换密码以保障账号安全。",
                style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFD97706),
            )
        }
    }
}

@Composable
private fun PasswordSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F5F9))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF475569),
        )
    }
}

@Composable
private fun PasswordInlineMessage(
    message: String,
    color: Color,
    background: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
    }
}

private fun evaluatePasswordStrength(password: String): PasswordStrength {
    val checks = listOf(
        password.length >= 8,
        password.any { it.isUpperCase() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() },
    )
    val score = checks.count { it }.coerceAtLeast(if (password.isBlank()) 0 else 1)
    return when {
        score >= 4 -> PasswordStrength(label = "强", color = PasswordSuccess, score = 4)
        score >= 2 -> PasswordStrength(label = "中", color = PasswordWarning, score = 2)
        score >= 1 -> PasswordStrength(label = "弱", color = Color(0xFFEF4444), score = 1)
        else -> PasswordStrength(label = "弱", color = Color(0xFFE5E7EB), score = 0)
    }
}
