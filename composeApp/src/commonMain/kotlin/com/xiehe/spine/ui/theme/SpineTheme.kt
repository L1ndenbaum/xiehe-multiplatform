package com.xiehe.spine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.currentHour24

@Immutable
enum class ThemeBrand {
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
    val brand: ThemeBrand = ThemeBrand.GREEN,
    val mode: ThemeMode = ThemeMode.SYSTEM,
)

@Immutable
data class SpineColors(
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
data class SpineTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val subhead: TextStyle,
    val caption: TextStyle,
)

@Immutable
data class SpineSpacing(
    val xs: androidx.compose.ui.unit.Dp,
    val sm: androidx.compose.ui.unit.Dp,
    val md: androidx.compose.ui.unit.Dp,
    val base: androidx.compose.ui.unit.Dp,
    val lg: androidx.compose.ui.unit.Dp,
    val xl: androidx.compose.ui.unit.Dp,
    val x2l: androidx.compose.ui.unit.Dp,
    val x3l: androidx.compose.ui.unit.Dp,
)

@Immutable
data class SpineRadius(
    val xs: androidx.compose.ui.unit.Dp,
    val sm: androidx.compose.ui.unit.Dp,
    val md: androidx.compose.ui.unit.Dp,
    val lg: androidx.compose.ui.unit.Dp,
    val xl: androidx.compose.ui.unit.Dp,
    val full: androidx.compose.ui.unit.Dp,
)

private val LocalSpineColors = compositionLocalOf { lightPalette(ThemeBrand.GREEN) }
private val LocalSpineTypography = compositionLocalOf { defaultTypography() }
private val LocalSpineSpacing = compositionLocalOf {
    SpineSpacing(
        xs = 2.dp,
        sm = 4.dp,
        md = 8.dp,
        base = 12.dp,
        lg = 14.dp,
        xl = 16.dp,
        x2l = 20.dp,
        x3l = 24.dp,
    )
}
private val LocalSpineRadius = compositionLocalOf {
    SpineRadius(
        xs = 4.dp,
        sm = 8.dp,
        md = 12.dp,
        lg = 16.dp,
        xl = 20.dp,
        full = 100.dp,
    )
}

object SpineTheme {
    val colors: SpineColors
        @Composable get() = LocalSpineColors.current

    val typography: SpineTypography
        @Composable get() = LocalSpineTypography.current

    val spacing: SpineSpacing
        @Composable get() = LocalSpineSpacing.current

    val radius: SpineRadius
        @Composable get() = LocalSpineRadius.current
}

@Composable
fun SpineTheme(
    preference: ThemePreference,
    content: @Composable () -> Unit,
) {
    val dark = when (preference.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.AUTO_TIME -> {
            val hour = currentHour24()
            hour >= 20 || hour < 7
        }
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = remember(preference.brand, dark) {
        if (dark) darkPalette(preference.brand) else lightPalette(preference.brand)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSpineColors provides colors,
        LocalSpineTypography provides defaultTypography(),
        LocalSpineSpacing provides LocalSpineSpacing.current,
        LocalSpineRadius provides LocalSpineRadius.current,
        content = content,
    )
}

private fun defaultTypography(): SpineTypography {
    val family = FontFamily.SansSerif
    return SpineTypography(
        display = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        title = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp),
        subhead = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 13.sp),
        caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    )
}

private fun lightPalette(brand: ThemeBrand): SpineColors {
    return when (brand) {
        ThemeBrand.GREEN -> SpineColors(
            primary = Color(0xFF3D8A5A),
            onPrimary = Color(0xFFFFFFFF),
            primaryMuted = Color(0xFFC8F0D8),
            background = Color(0xFFF5F4F1),
            backgroundElevated = Color(0xFFFAFAF8),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFEDECEA),
            borderSubtle = Color(0xFFE5E4E1),
            borderStrong = Color(0xFFD1D0CD),
            textPrimary = Color(0xFF1A1918),
            textSecondary = Color(0xFF6D6C6A),
            textTertiary = Color(0xFF9C9B99),
            success = Color(0xFF4D9B6A),
            warning = Color(0xFFD4A64A),
            error = Color(0xFFD08068),
            info = Color(0xFF3D8A5A),
            tabInactive = Color(0xFFA8A7A5),
        )

        ThemeBrand.BLUE -> SpineColors(
            primary = Color(0xFF2F6EA8),
            onPrimary = Color(0xFFFFFFFF),
            primaryMuted = Color(0xFFD0E7FF),
            background = Color(0xFFF3F6FA),
            backgroundElevated = Color(0xFFF8FAFD),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFE9EEF5),
            borderSubtle = Color(0xFFDCE4EE),
            borderStrong = Color(0xFFBFCBDA),
            textPrimary = Color(0xFF151A20),
            textSecondary = Color(0xFF5C6775),
            textTertiary = Color(0xFF8B96A3),
            success = Color(0xFF3A8E67),
            warning = Color(0xFFD6A845),
            error = Color(0xFFD26D6D),
            info = Color(0xFF2F6EA8),
            tabInactive = Color(0xFF9CA8B8),
        )
    }
}

private fun darkPalette(brand: ThemeBrand): SpineColors {
    return when (brand) {
        ThemeBrand.GREEN -> SpineColors(
            primary = Color(0xFF69B885),
            onPrimary = Color(0xFF102016),
            primaryMuted = Color(0xFF1F3A2A),
            background = Color(0xFF171A18),
            backgroundElevated = Color(0xFF1B201D),
            surface = Color(0xFF202622),
            surfaceMuted = Color(0xFF26302A),
            borderSubtle = Color(0xFF334039),
            borderStrong = Color(0xFF45544A),
            textPrimary = Color(0xFFE8ECE8),
            textSecondary = Color(0xFFB8C2BC),
            textTertiary = Color(0xFF87948C),
            success = Color(0xFF65C28A),
            warning = Color(0xFFE0B55C),
            error = Color(0xFFE08E79),
            info = Color(0xFF79C6A2),
            tabInactive = Color(0xFF7E8B83),
        )

        ThemeBrand.BLUE -> SpineColors(
            primary = Color(0xFF73A9E0),
            onPrimary = Color(0xFF0E1A27),
            primaryMuted = Color(0xFF1A3045),
            background = Color(0xFF151920),
            backgroundElevated = Color(0xFF1A1F28),
            surface = Color(0xFF1E2530),
            surfaceMuted = Color(0xFF273142),
            borderSubtle = Color(0xFF334257),
            borderStrong = Color(0xFF445974),
            textPrimary = Color(0xFFEAF0F7),
            textSecondary = Color(0xFFB3C0D0),
            textTertiary = Color(0xFF8395AA),
            success = Color(0xFF5CB38A),
            warning = Color(0xFFE2B75D),
            error = Color(0xFFE07D7D),
            info = Color(0xFF8EC2F5),
            tabInactive = Color(0xFF798A9F),
        )
    }
}
