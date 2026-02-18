package com.xiehe.spine.ui.theme

import androidx.compose.ui.graphics.Color

internal fun lightPalette(brand: AppThemeBrandColor): SpineAppColors {
    return when (brand) {
        AppThemeBrandColor.GREEN -> SpineAppColors(
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

        AppThemeBrandColor.BLUE -> SpineAppColors(
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

internal fun darkPalette(brand: AppThemeBrandColor): SpineAppColors {
    return when (brand) {
        AppThemeBrandColor.GREEN -> SpineAppColors(
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

        AppThemeBrandColor.BLUE -> SpineAppColors(
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
