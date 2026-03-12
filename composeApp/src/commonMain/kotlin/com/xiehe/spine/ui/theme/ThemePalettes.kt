package com.xiehe.spine.ui.theme

import androidx.compose.ui.graphics.Color

internal fun lightPalette(brand: AppThemeBrandColor): SpineAppColors {
    val primary = when (brand) {
        AppThemeBrandColor.PURPLE -> Color(0xFF7C3AED)
        AppThemeBrandColor.BLUE -> Color(0xFF2563EB)
        AppThemeBrandColor.GREEN -> Color(0xFF059669)
    }
    val primaryMuted = when (brand) {
        AppThemeBrandColor.PURPLE -> Color(0xFFEDE9FE)
        AppThemeBrandColor.BLUE -> Color(0xFFDBEAFE)
        AppThemeBrandColor.GREEN -> Color(0xFFD1FAE5)
    }

    return SpineAppColors(
        isDark = false,
        primary = primary,
        onPrimary = Color(0xFFFFFFFF),
        primaryMuted = primaryMuted,
        background = Color(0xFFF8FAFC),
        backgroundElevated = Color(0xFFF1F5F9),
        surface = Color(0xFFFFFFFF),
        surfaceMuted = Color(0xFFF8FAFC),
        borderSubtle = Color(0xFFE2E8F0),
        borderStrong = Color(0xFFCBD5E1),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569),
        textTertiary = Color(0xFF94A3B8),
        success = Color(0xFF10B981),
        warning = Color(0xFFF97316),
        error = Color(0xFFEF4444),
        info = Color(0xFF0EA5E9),
        tabInactive = Color(0xFF94A3B8),
    )
}

internal fun darkPalette(brand: AppThemeBrandColor): SpineAppColors {
    val primary = when (brand) {
        AppThemeBrandColor.PURPLE -> Color(0xFF8B5CF6)
        AppThemeBrandColor.BLUE -> Color(0xFF3B82F6)
        AppThemeBrandColor.GREEN -> Color(0xFF10B981)
    }
    val primaryMuted = when (brand) {
        AppThemeBrandColor.PURPLE -> Color(0xFF312049)
        AppThemeBrandColor.BLUE -> Color(0xFF172554)
        AppThemeBrandColor.GREEN -> Color(0xFF052E2B)
    }

    return SpineAppColors(
        isDark = true,
        primary = primary,
        onPrimary = Color(0xFFFFFFFF),
        primaryMuted = primaryMuted,
        background = Color(0xFF020617),
        backgroundElevated = Color(0xFF0F172A),
        surface = Color(0xFF111827),
        surfaceMuted = Color(0xFF0F172A),
        borderSubtle = Color(0xFF1F2937),
        borderStrong = Color(0xFF334155),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFCBD5E1),
        textTertiary = Color(0xFF94A3B8),
        success = Color(0xFF34D399),
        warning = Color(0xFFFB923C),
        error = Color(0xFFF87171),
        info = Color(0xFF38BDF8),
        tabInactive = Color(0xFF94A3B8),
    )
}

fun previewBrandPrimaryColor(
    brand: AppThemeBrandColor,
    isDark: Boolean,
): Color = if (isDark) darkPalette(brand).primary else lightPalette(brand).primary
