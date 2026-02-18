package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.store.ThemePreferenceRepository
import com.xiehe.spine.ui.theme.AppThemeBrandColor
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.theme.ThemePreference
import kotlinx.coroutines.flow.StateFlow

class AppearanceViewModel(
    private val repository: ThemePreferenceRepository,
) {
    val state: StateFlow<ThemePreference> = repository.preference

    fun updateBrand(brand: AppThemeBrandColor) {
        repository.updateBrand(brand)
    }

    fun updateMode(mode: ThemeMode) {
        repository.updateMode(mode)
    }
}
