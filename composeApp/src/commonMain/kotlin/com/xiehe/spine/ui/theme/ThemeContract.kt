package com.xiehe.spine.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Immutable
enum class AppThemeBrandColor {
    PURPLE,
    GREEN,
    BLUE,
}

@Immutable
enum class ThemeMode {
    SYSTEM,
    AUTO_TIME,
    LIGHT,
    DARK,
}

@Immutable
data class ThemePreference(
    val brand: AppThemeBrandColor = AppThemeBrandColor.PURPLE,
    val mode: ThemeMode = ThemeMode.SYSTEM,
)

@Immutable
data class SpineExamCategoryStyle(
    val background: Color,
    val content: Color,
    val accentStart: Color,
    val accentEnd: Color,
)

@Immutable
data class SpineExamCategoryColors(
    val front: SpineExamCategoryStyle,
    val side: SpineExamCategoryStyle,
    val leftBending: SpineExamCategoryStyle,
    val rightBending: SpineExamCategoryStyle,
    val posturePhoto: SpineExamCategoryStyle,
)

@Immutable
data class SpineAppColors(
    val isDark: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val primaryMuted: Color,
    val headerHighlight: Color,
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val tabInactive: Color,
    val examCategories: SpineExamCategoryColors,
)

fun SpineExamCategoryColors.resolve(examType: String): SpineExamCategoryStyle = when (examType.trim()) {
    "正位X光片" -> front
    "侧位X光片" -> side
    "左侧曲位" -> leftBending
    "右侧曲位" -> rightBending
    "体态照片" -> posturePhoto
    else -> front
}

@Immutable
data class SpineAppTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val subhead: TextStyle,
    val caption: TextStyle,
)

@Immutable
data class SpineAppSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val base: Dp,
    val lg: Dp,
    val xl: Dp,
    val x2l: Dp,
    val x3l: Dp,
)

@Immutable
data class SpineAppRadius(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val full: Dp,
)
