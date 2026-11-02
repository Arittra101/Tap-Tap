package com.taptap.ui.settings

import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode

/** Settings (FR-6.1 - FR-6.5). */
data class SettingsState(
    val color: ThemeColor = ThemeColor.PINK,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "en",
    val vibration: Boolean = true,
    val sound: Boolean = false,
    /** FR-6.2: true when the phone's own setting is dark, for the result note. */
    val phoneIsDark: Boolean = false,
)

sealed interface SettingsIntent {
    data class ChangeColor(val color: ThemeColor) : SettingsIntent
    data class ChangeMode(val mode: ThemeMode) : SettingsIntent
    data class ChangeLanguage(val tag: String) : SettingsIntent
    data class ToggleVibration(val on: Boolean) : SettingsIntent
    data class ToggleSound(val on: Boolean) : SettingsIntent
    data object OpenPrivacyPolicy : SettingsIntent
}

sealed interface SettingsEffect {
    /** FR-8.2: apply the per-app locale outside the ViewModel. */
    data class ApplyLanguage(val tag: String) : SettingsEffect
    data object OpenPrivacyPolicy : SettingsEffect
}
