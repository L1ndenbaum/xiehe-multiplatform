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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.AppThemeBrandColor
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.viewmodel.profile.AppearanceViewModel

private val SettingsBackground = Color(0xFFF1F5F9)
private val SettingsCardBorder = Color(0xFFE2E8F0)
private val SettingsCardShadow = Color(0x120F172A)
private val SettingsTitle = Color(0xFF0F172A)
private val SettingsBody = Color(0xFF94A3B8)
private val SettingsPurple = Color(0xFF8B5CF6)

private enum class SettingsPickerType {
    LANGUAGE,
    TIMEZONE,
}

@Composable
fun AppearanceScreen(vm: AppearanceViewModel) {
    val preference by vm.state.collectAsState()
    var selectedBrand by remember(preference.brand) { mutableStateOf(preference.brand) }
    var darkModeEnabled by remember(preference.mode) { mutableStateOf(preference.mode == ThemeMode.DARK) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var autoSaveEnabled by rememberSaveable { mutableStateOf(false) }
    var language by rememberSaveable { mutableStateOf("简体中文") }
    var timezone by rememberSaveable { mutableStateOf("北京时间 (UTC+8)") }
    var pickerType by remember { mutableStateOf<SettingsPickerType?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(preference.brand, preference.mode) {
        selectedBrand = preference.brand
        darkModeEnabled = preference.mode == ThemeMode.DARK
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsBrandCard(
            selectedBrand = selectedBrand,
            onBrandSelected = {
                selectedBrand = it
                successMessage = null
            },
        )

        SettingsSectionCard(title = "界面设置") {
            SettingsToggleRow(
                iconGlyph = IconToken.EYE,
                iconTint = SettingsPurple,
                iconBackground = Color(0xFFF1F5F9),
                title = "启用暗色主题",
                subtitle = "切换深色界面风格",
                checked = darkModeEnabled,
                onCheckedChange = {
                    darkModeEnabled = it
                    successMessage = null
                },
            )
            SettingsDivider()
            SettingsToggleRow(
                iconGlyph = IconToken.BELL,
                iconTint = SettingsPurple,
                iconBackground = Color(0xFFF5F3FF),
                title = "显示系统通知",
                subtitle = "接收消息和提醒推送",
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    successMessage = null
                },
            )
            SettingsDivider()
            SettingsToggleRow(
                iconGlyph = IconToken.SAVE,
                iconTint = Color(0xFFF59E0B),
                iconBackground = Color(0xFFFFFBEB),
                title = "自动保存草稿",
                subtitle = "编辑内容自动暂存",
                checked = autoSaveEnabled,
                onCheckedChange = {
                    autoSaveEnabled = it
                    successMessage = null
                },
            )
        }

        SettingsSectionCard(title = "语言和地区") {
            SettingsFieldLabel(text = "语言", iconGlyph = IconToken.MESSAGE, iconTint = Color(0xFF3B82F6), iconBackground = Color(0xFFEFF6FF))
            SettingsDropdownField(
                text = language,
                onClick = {
                    pickerType = SettingsPickerType.LANGUAGE
                    successMessage = null
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFieldLabel(text = "时区", iconGlyph = IconToken.CLOCK, iconTint = Color(0xFF10B981), iconBackground = Color(0xFFECFDF5))
            SettingsDropdownField(
                text = timezone,
                onClick = {
                    pickerType = SettingsPickerType.TIMEZONE
                    successMessage = null
                },
            )
        }

        successMessage?.let {
            SettingsInlineMessage(it)
        }

        Button(
            text = "保存设置",
            onClick = {
                vm.updateBrand(selectedBrand)
                vm.updateMode(if (darkModeEnabled) ThemeMode.DARK else ThemeMode.LIGHT)
                successMessage = buildString {
                    append("设置已保存")
                    if (!notificationsEnabled) append("，通知已关闭")
                    if (autoSaveEnabled) append("，已启用自动保存")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    pickerType?.let { picker ->
        val title = if (picker == SettingsPickerType.LANGUAGE) "选择语言" else "选择时区"
        val options = if (picker == SettingsPickerType.LANGUAGE) {
            listOf("简体中文", "English")
        } else {
            listOf("北京时间 (UTC+8)", "东京时间 (UTC+9)", "新加坡时间 (UTC+8)")
        }
        SettingsPickerDialog(
            title = title,
            options = options,
            currentValue = if (picker == SettingsPickerType.LANGUAGE) language else timezone,
            onDismiss = { pickerType = null },
            onSelect = {
                if (picker == SettingsPickerType.LANGUAGE) {
                    language = it
                } else {
                    timezone = it
                }
            },
        )
    }
}

@Composable
private fun SettingsBrandCard(
    selectedBrand: AppThemeBrandColor,
    onBrandSelected: (AppThemeBrandColor) -> Unit,
) {
    SettingsSectionCard(title = "主色体系") {
        Text(
            text = "选择应用品牌色",
            style = com.xiehe.spine.ui.theme.SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = SettingsBody,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsBrandDot(
                color = SettingsPurple,
                selected = selectedBrand == AppThemeBrandColor.PURPLE,
                onClick = { onBrandSelected(AppThemeBrandColor.PURPLE) },
            )
            SettingsBrandDot(
                color = Color(0xFF3B82F6),
                selected = selectedBrand == AppThemeBrandColor.BLUE,
                onClick = { onBrandSelected(AppThemeBrandColor.BLUE) },
            )
            SettingsBrandDot(
                color = Color(0xFF10B981),
                selected = selectedBrand == AppThemeBrandColor.GREEN,
                onClick = { onBrandSelected(AppThemeBrandColor.GREEN) },
            )
        }
    }
}

@Composable
private fun SettingsBrandDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(if (selected) 10.dp else 0.dp, CircleShape, ambientColor = color.copy(alpha = 0.3f), spotColor = color.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 4.dp else 0.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .shadow(14.dp, shape, ambientColor = SettingsCardShadow, spotColor = SettingsCardShadow)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, SettingsCardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = {
            Text(
                text = title,
                style = com.xiehe.spine.ui.theme.SpineTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = SettingsBody,
            )
            content()
        },
    )
}

@Composable
private fun SettingsToggleRow(
    iconGlyph: IconToken,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(glyph = iconGlyph, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = com.xiehe.spine.ui.theme.SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                    color = SettingsTitle,
                )
                Text(
                    text = subtitle,
                    style = com.xiehe.spine.ui.theme.SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
                    color = SettingsBody,
                )
            }
        }
        SettingsSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) SettingsPurple else Color(0xFFE2E8F0))
            .clickable(onClick = { onCheckedChange(!checked) })
            .padding(horizontal = 3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun SettingsFieldLabel(
    text: String,
    iconGlyph: IconToken,
    iconTint: Color,
    iconBackground: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = iconGlyph, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Text(
            text = text,
            style = com.xiehe.spine.ui.theme.SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = SettingsTitle,
        )
    }
}

@Composable
private fun SettingsDropdownField(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, SettingsCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = com.xiehe.spine.ui.theme.SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = SettingsTitle,
        )
        AppIcon(glyph = IconToken.CHEVRON_DOWN, tint = SettingsBody, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFEEF2F7)),
    )
}

@Composable
private fun SettingsInlineMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 620.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFECFDF5))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = message,
            style = com.xiehe.spine.ui.theme.SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF059669),
        )
    }
}

@Composable
private fun SettingsPickerDialog(
    title: String,
    options: List<String>,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    PickerDialog(
        title = title,
        onDismissRequest = onDismiss,
        showActionRow = false,
    ) { dismiss ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (option == currentValue) Color(0xFFF5F3FF) else Color(0xFFF8FAFC))
                        .border(
                            1.dp,
                            if (option == currentValue) Color(0xFFE9D5FF) else SettingsCardBorder,
                            RoundedCornerShape(14.dp),
                        )
                        .clickable {
                            onSelect(option)
                            dismiss()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option,
                        style = com.xiehe.spine.ui.theme.SpineTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                        color = SettingsTitle,
                    )
                    if (option == currentValue) {
                        Text(
                            text = "✓",
                            style = com.xiehe.spine.ui.theme.SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                            color = SettingsPurple,
                        )
                    }
                }
            }
        }
    }
}
