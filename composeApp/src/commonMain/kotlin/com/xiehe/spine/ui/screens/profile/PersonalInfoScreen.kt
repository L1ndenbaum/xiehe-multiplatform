package com.xiehe.spine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.auth.AuthRepository
import com.xiehe.spine.ui.components.card.profile.ProfileTag
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.profile.PersonalInfoViewModel

private val ProfileInfoBackground = Color(0xFFF1F5F9)
private val ProfileInfoCardBorder = Color(0xFFE2E8F0)
private val ProfileInfoCardShadow = Color(0x120F172A)

private enum class PersonalInfoEditField {
    NAME,
    ROLE,
    PHONE,
}

@Composable
fun PersonalInfoScreen(
    vm: PersonalInfoViewModel,
    session: UserSession,
    authRepository: AuthRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()
    val scrollState = rememberScrollState()
    var editingField by remember { mutableStateOf<PersonalInfoEditField?>(null) }
    var localNotice by remember { mutableStateOf<String?>(null) }
    val cardShape = RoundedCornerShape(24.dp)

    LaunchedEffect(session.accessToken) {
        vm.seedFromSession(session)
        vm.load(
            session = session,
            repository = authRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    val displayName = state.realName.ifBlank { state.username.ifBlank { session.fullName ?: session.username } }
    val roleLabel = state.title.ifBlank { state.position.ifBlank { "医生" } }
    val departmentLabel = state.department.ifBlank { "未设置" }
    val emailLabel = state.email.ifBlank { "未设置" }
    val phoneLabel = state.phone.ifBlank { "未设置" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileInfoBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .shadow(16.dp, cardShape, ambientColor = ProfileInfoCardShadow, spotColor = ProfileInfoCardShadow)
                    .clip(cardShape)
                    .background(Color.White)
                    .border(1.dp, ProfileInfoCardBorder, cardShape)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x408B5CF6), spotColor = Color(0x408B5CF6))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(glyph = IconToken.USER_ROUND, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))))
                            .border(2.dp, Color.White, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(glyph = IconToken.IMAGE, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }

                Text(
                    text = displayName,
                    style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileTag(text = roleLabel, active = true)
                    ProfileTag(text = "已认证", active = false)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .shadow(16.dp, cardShape, ambientColor = ProfileInfoCardShadow, spotColor = ProfileInfoCardShadow)
                    .clip(cardShape)
                    .background(Color.White)
                    .border(1.dp, ProfileInfoCardBorder, cardShape),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 18.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))),
                    )
                    Text(
                        text = "基本资料",
                        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ProfileInfoCardBorder),
                )

                ProfileInfoRow(
                    label = "姓名",
                    value = displayName,
                    glyph = IconToken.PROFILE,
                    editable = true,
                    onClick = {
                        localNotice = null
                        editingField = PersonalInfoEditField.NAME
                    },
                )
                ProfileInfoRow(
                    label = "职位",
                    value = roleLabel,
                    glyph = IconToken.CHECK,
                    editable = true,
                    onClick = {
                        localNotice = null
                        editingField = PersonalInfoEditField.ROLE
                    },
                )
                ProfileInfoRow(
                    label = "科室",
                    value = departmentLabel,
                    glyph = IconToken.PATIENTS,
                    editable = false,
                    onClick = { localNotice = "当前版本暂不支持修改科室信息" },
                )
                ProfileInfoRow(
                    label = "邮箱",
                    value = emailLabel,
                    glyph = IconToken.MESSAGE,
                    editable = false,
                    onClick = { localNotice = "当前版本暂不支持修改邮箱" },
                )
                ProfileInfoRow(
                    label = "手机号",
                    value = phoneLabel,
                    glyph = IconToken.MESSAGE,
                    editable = true,
                    onClick = {
                        localNotice = null
                        editingField = PersonalInfoEditField.PHONE
                    },
                    showDivider = false,
                )
            }

            localNotice?.let {
                InlineMessageCard(message = it, color = Color(0xFF7C3AED), background = Color(0xFFF5F3FF))
            }
            state.errorMessage?.let {
                InlineMessageCard(message = it, color = Color(0xFFDC2626), background = Color(0xFFFEF2F2))
            }
            state.successMessage?.let {
                InlineMessageCard(message = it, color = Color(0xFF059669), background = Color(0xFFECFDF5))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        editingField?.let { field ->
            ProfileFieldEditorDialog(
                title = when (field) {
                    PersonalInfoEditField.NAME -> "编辑姓名"
                    PersonalInfoEditField.ROLE -> "编辑职位"
                    PersonalInfoEditField.PHONE -> "编辑手机号"
                },
                initialValue = when (field) {
                    PersonalInfoEditField.NAME -> state.realName
                    PersonalInfoEditField.ROLE -> state.title.ifBlank { state.position }
                    PersonalInfoEditField.PHONE -> state.phone
                },
                placeholder = when (field) {
                    PersonalInfoEditField.NAME -> "请输入姓名"
                    PersonalInfoEditField.ROLE -> "请输入职位"
                    PersonalInfoEditField.PHONE -> "请输入手机号"
                },
                onDismiss = { editingField = null },
                onConfirm = { value ->
                    when (field) {
                        PersonalInfoEditField.NAME -> vm.updateRealName(value)
                        PersonalInfoEditField.ROLE -> {
                            if (state.title.isBlank() && state.position.isNotBlank()) {
                                vm.updatePosition(value)
                            } else {
                                vm.updateTitle(value)
                            }
                        }
                        PersonalInfoEditField.PHONE -> vm.updatePhone(value)
                    }
                    vm.save(
                        session = session,
                        repository = authRepository,
                        onSessionUpdated = onSessionUpdated,
                    )
                },
            )
        }

        if (state.loading || state.saving) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    glyph: IconToken,
    editable: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F3FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(glyph = glyph, tint = Color(0xFF8B5CF6), modifier = Modifier.size(15.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF94A3B8),
                    )
                    Text(
                        text = value,
                        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, ProfileInfoCardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    glyph = if (editable) IconToken.EDIT else IconToken.CHEVRON_RIGHT,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9)),
            )
        }
    }
}

@Composable
private fun ProfileFieldEditorDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember(initialValue) { mutableStateOf(initialValue) }

    PickerDialog(
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(draft.trim()) },
    ) {
        TextField(
            value = draft,
            onValueChange = { draft = it },
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            leadingGlyph = IconToken.EDIT,
        )
    }
}

@Composable
private fun InlineMessageCard(
    message: String,
    color: Color,
    background: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .border(1.dp, color.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = message,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
            color = color,
        )
    }
}
