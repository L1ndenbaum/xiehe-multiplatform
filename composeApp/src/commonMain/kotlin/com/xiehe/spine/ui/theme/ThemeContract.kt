package com.xiehe.spine.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Immutable
enum class AppThemeBrandColor {
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
    val brand: AppThemeBrandColor = AppThemeBrandColor.GREEN,
    val mode: ThemeMode = ThemeMode.SYSTEM,
)

@Immutable
data class SpineAppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryMuted: Color,
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
)

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
