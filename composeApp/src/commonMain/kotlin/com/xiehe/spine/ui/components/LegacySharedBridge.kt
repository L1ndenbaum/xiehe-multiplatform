package com.xiehe.spine.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.xiehe.spine.ui.components.avatar.shared.Avatar as SharedAvatar
import com.xiehe.spine.ui.components.badge.shared.IconBadge as SharedIconBadge
import com.xiehe.spine.ui.components.button.shared.Button as SharedButton
import com.xiehe.spine.ui.components.button.shared.CompactButton as SharedCompactButton
import com.xiehe.spine.ui.components.card.shared.Card as SharedCard
import com.xiehe.spine.ui.components.card.shared.OperationVerifyCard as SharedOperationVerifyCard
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay as SharedLoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.PeriodicTaskTrigger as SharedPeriodicTaskTrigger
import com.xiehe.spine.ui.components.feedback.shared.Text as SharedText
import com.xiehe.spine.ui.components.form.input.FilterSelector as SharedFilterSelector
import com.xiehe.spine.ui.components.form.input.SelectablePill as SharedSelectablePill
import com.xiehe.spine.ui.components.form.input.TextField as SharedTextField
import com.xiehe.spine.ui.components.form.picker.DatePickerField as SharedDatePickerField
import com.xiehe.spine.ui.components.form.picker.DateWheelPickerDialog as SharedDateWheelPickerDialog
import com.xiehe.spine.ui.components.form.picker.PickerDialog as SharedPickerDialog
import com.xiehe.spine.ui.components.form.picker.WheelPickerColumn as SharedWheelPickerColumn
import com.xiehe.spine.ui.components.icon.shared.AppIcon as SharedAppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken as SharedIconToken
import com.xiehe.spine.ui.components.icon.shared.MiniBarChart as SharedMiniBarChart
import com.xiehe.spine.ui.components.icon.shared.ProgressRing as SharedProgressRing
import com.xiehe.spine.ui.theme.SpineTheme

typealias IconToken = SharedIconToken

@Composable
fun AppIcon(
    glyph: IconToken,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.textSecondary,
) = SharedAppIcon(glyph = glyph, modifier = modifier, tint = tint)

@Composable
fun MiniBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    accentColor: Color = SpineTheme.colors.primary,
) = SharedMiniBarChart(values = values, labels = labels, modifier = modifier, accentColor = accentColor)

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.primary,
) = SharedProgressRing(progress = progress, modifier = modifier, tint = tint)

@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = SharedCard(modifier = modifier, content = content)

@Composable
fun OperationVerifyCard(
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "操作确认",
    confirmText: String = "确认",
    cancelText: String = "取消",
    confirmButtonColor: Color = SpineTheme.colors.error,
    cancelButtonColor: Color = SpineTheme.colors.textSecondary,
    confirmTextColor: Color = SpineTheme.colors.onPrimary,
    cancelTextColor: Color = SpineTheme.colors.onPrimary,
) = SharedOperationVerifyCard(
    message = message,
    onConfirm = onConfirm,
    onCancel = onCancel,
    modifier = modifier,
    title = title,
    confirmText = confirmText,
    cancelText = cancelText,
    confirmButtonColor = confirmButtonColor,
    cancelButtonColor = cancelButtonColor,
    confirmTextColor = confirmTextColor,
    cancelTextColor = cancelTextColor,
)

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingGlyph: IconToken? = null,
    customContainerColor: Color? = null,
    customContentColor: Color? = null,
) = SharedButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    leadingGlyph = leadingGlyph,
    customContainerColor = customContainerColor,
    customContentColor = customContentColor,
)

@Composable
fun CompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SpineTheme.colors.primary,
    contentColor: Color = SpineTheme.colors.onPrimary,
) = SharedCompactButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    containerColor = containerColor,
    contentColor = contentColor,
)

@Composable
fun IconBadge(
    glyph: IconToken,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    iconSize: Dp = Dp.Unspecified,
    cornerRadius: Dp = Dp.Unspecified,
    colors: List<Color> = listOf(SpineTheme.colors.primary, SpineTheme.colors.primary.copy(alpha = 0.86f)),
    iconTint: Color = SpineTheme.colors.onPrimary,
    shadowColor: Color = colors.firstOrNull()?.copy(alpha = 0.24f) ?: SpineTheme.colors.primary.copy(alpha = 0.24f),
) = SharedIconBadge(
    glyph = glyph,
    modifier = modifier,
    size = if (size == Dp.Unspecified) Dp(56f) else size,
    iconSize = if (iconSize == Dp.Unspecified) Dp(20f) else iconSize,
    cornerRadius = if (cornerRadius == Dp.Unspecified) Dp(20f) else cornerRadius,
    colors = colors,
    iconTint = iconTint,
    shadowColor = shadowColor,
)

@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = Dp(44f),
    avatarPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    avatarResource: org.jetbrains.compose.resources.DrawableResource? = null,
) = SharedAvatar(
    name = name,
    modifier = modifier,
    size = size,
    avatarPainter = avatarPainter,
    avatarResource = avatarResource,
)

@Composable
fun LoadingOverlay(
    message: String = "正在加载中...",
    modifier: Modifier = Modifier,
) = SharedLoadingOverlay(message = message, modifier = modifier)

@Composable
fun PeriodicTaskTrigger(
    intervalMillis: Long,
    enabled: Boolean,
    runImmediately: Boolean = true,
    key: Any? = Unit,
    task: suspend () -> Unit,
) = SharedPeriodicTaskTrigger(intervalMillis = intervalMillis, enabled = enabled, runImmediately = runImmediately, key = key, task = task)

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SpineTheme.typography.body,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
) = SharedText(text = text, modifier = modifier, style = style, color = color, maxLines = maxLines)

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    leadingGlyph: IconToken? = null,
    trailingGlyph: IconToken? = null,
    onTrailingClick: (() -> Unit)? = null,
) = SharedTextField(
    value = value,
    onValueChange = onValueChange,
    placeholder = placeholder,
    modifier = modifier,
    password = password,
    singleLine = singleLine,
    readOnly = readOnly,
    leadingGlyph = leadingGlyph,
    trailingGlyph = trailingGlyph,
    onTrailingClick = onTrailingClick,
)

@Composable
fun FilterSelector(
    text: String,
    modifier: Modifier = Modifier,
    leadingGlyph: IconToken,
    onClick: () -> Unit,
) = SharedFilterSelector(text = text, modifier = modifier, leadingGlyph = leadingGlyph, onClick = onClick)

@Composable
fun SelectablePill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) = SharedSelectablePill(text = text, selected = selected, modifier = modifier, onClick = onClick)

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) = SharedDatePickerField(value = value, onValueChange = onValueChange, modifier = modifier)

@Composable
fun DateWheelPickerDialog(
    initialValue: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) = SharedDateWheelPickerDialog(initialValue = initialValue, onDismissRequest = onDismissRequest, onConfirm = onConfirm)

@Composable
fun PickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showActionRow: Boolean = true,
    onConfirm: (() -> Unit)? = null,
    maxDialogWidth: Dp? = null,
    maxDialogHeightFraction: Float? = null,
    overlayMaxAlpha: Float = 0.26f,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) = SharedPickerDialog(
    title = title,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    showActionRow = showActionRow,
    onConfirm = onConfirm,
    maxDialogWidth = maxDialogWidth,
    maxDialogHeightFraction = maxDialogHeightFraction,
    overlayMaxAlpha = overlayMaxAlpha,
    content = content,
)

@Composable
fun WheelPickerColumn(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelectedIndexChange: (Int) -> Unit,
) = SharedWheelPickerColumn(options = options, selectedIndex = selectedIndex, modifier = modifier, onSelectedIndexChange = onSelectedIndexChange)