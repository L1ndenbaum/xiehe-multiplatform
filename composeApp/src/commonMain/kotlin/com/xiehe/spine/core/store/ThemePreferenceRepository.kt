package com.xiehe.spine.core.store

import com.xiehe.spine.ui.theme.ThemeBrand
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferenceRepository(
    private val store: KeyValueStore,
) {
    private val preferenceKey = "theme_preference"

    private val _preference = MutableStateFlow(loadPreference())
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    fun updateBrand(brand: ThemeBrand) {
        val next = _preference.value.copy(brand = brand)
        persist(next)
    }

    fun updateMode(mode: ThemeMode) {
        val next = _preference.value.copy(mode = mode)
        persist(next)
    }

    private fun persist(value: ThemePreference) {
        store.putString(preferenceKey, "${value.brand.name}|${value.mode.name}")
        _preference.value = value
    }

    private fun loadPreference(): ThemePreference {
        val raw = store.getString(preferenceKey) ?: return ThemePreference()
        val parts = raw.split('|')
        val brand = ThemeBrand.entries.firstOrNull { it.name == parts.getOrNull(0) } ?: ThemeBrand.GREEN
        val mode = ThemeMode.entries.firstOrNull { it.name == parts.getOrNull(1) } ?: ThemeMode.SYSTEM
        return ThemePreference(brand = brand, mode = mode)
    }
}
