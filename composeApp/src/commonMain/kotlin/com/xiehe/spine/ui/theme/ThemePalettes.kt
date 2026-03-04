package com.xiehe.spine.ui.theme

import androidx.compose.ui.graphics.Color

internal fun lightPalette(brand: AppThemeBrandColor): SpineAppColors {
    return when (brand) {
        AppThemeBrandColor.GREEN -> SpineAppColors(
            primary = Color(0xFF7C3AED),
            onPrimary = Color(0xFFFFFFFF),
            primaryMuted = Color(0xFFEDE6FF),
            background = Color(0xFFF8FAFC),
            backgroundElevated = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFF1F5F9),
            borderSubtle = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            textPrimary = Color(0xFF1E293B),
            textSecondary = Color(0xFF64748B),
            textTertiary = Color(0xFF94A3B8),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            error = Color(0xFFEF4444),
            info = Color(0xFF7C3AED),
            tabInactive = Color(0xFF94A3B8),
        )

        AppThemeBrandColor.BLUE -> SpineAppColors(
            primary = Color(0xFF4F46E5),
            onPrimary = Color(0xFFFFFFFF),
            primaryMuted = Color(0xFFE5E7FF),
            background = Color(0xFFF8FAFC),
            backgroundElevated = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFF1F5F9),
            borderSubtle = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            textPrimary = Color(0xFF1E293B),
            textSecondary = Color(0xFF64748B),
            textTertiary = Color(0xFF94A3B8),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            error = Color(0xFFEF4444),
            info = Color(0xFF4F46E5),
            tabInactive = Color(0xFF94A3B8),
        )
    }
}

internal fun darkPalette(brand: AppThemeBrandColor): SpineAppColors {
    return when (brand) {
        AppThemeBrandColor.GREEN -> SpineAppColors(
            primary = Color(0xFFA78BFA),
            onPrimary = Color(0xFF1A1325),
            primaryMuted = Color(0xFF312049),
            background = Color(0xFF0F172A),
            backgroundElevated = Color(0xFF111C32),
            surface = Color(0xFF172339),
            surfaceMuted = Color(0xFF1E2C46),
            borderSubtle = Color(0xFF2D3C5A),
            borderStrong = Color(0xFF3C4F73),
            textPrimary = Color(0xFFE2E8F0),
            textSecondary = Color(0xFFB8C2D3),
            textTertiary = Color(0xFF8FA0B8),
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            error = Color(0xFFF87171),
            info = Color(0xFFA78BFA),
            tabInactive = Color(0xFF7F90A9),
        )

        AppThemeBrandColor.BLUE -> SpineAppColors(
            primary = Color(0xFF818CF8),
            onPrimary = Color(0xFF111827),
            primaryMuted = Color(0xFF242C50),
            background = Color(0xFF0F172A),
            backgroundElevated = Color(0xFF111C32),
            surface = Color(0xFF172339),
            surfaceMuted = Color(0xFF1E2C46),
            borderSubtle = Color(0xFF2D3C5A),
            borderStrong = Color(0xFF3C4F73),
            textPrimary = Color(0xFFE2E8F0),
            textSecondary = Color(0xFFB8C2D3),
            textTertiary = Color(0xFF8FA0B8),
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            error = Color(0xFFF87171),
            info = Color(0xFF818CF8),
            tabInactive = Color(0xFF7F90A9),
        )
    }
}
