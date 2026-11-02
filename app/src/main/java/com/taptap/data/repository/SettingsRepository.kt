package com.taptap.data.repository

import com.taptap.data.settings.SettingsStore
import com.taptap.model.AppSettings
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * The app's settings, as one stream plus one setter per key (tech spec 7.4).
 *
 * [SUPPORTED_LANGUAGES] is the list from FR-6.3, in the order Settings shows them.
 */
class SettingsRepository(private val store: SettingsStore) {

    val settings: Flow<AppSettings> = store.settings

    suspend fun setThemeColor(value: ThemeColor) = store.setThemeColor(value)

    suspend fun setThemeMode(value: ThemeMode) = store.setThemeMode(value)

    suspend fun setLanguageTag(tag: String) = store.setLanguageTag(tag)

    suspend fun setVibration(enabled: Boolean) = store.setVibration(enabled)

    suspend fun setSound(enabled: Boolean) = store.setSound(enabled)

    /**
     * The tag to actually apply.
     *
     * An empty stored value means the user never chose: follow the phone when it speaks
     * one of the six, else English (requirements 6).
     */
    fun resolveLanguageTag(stored: String, phoneLanguage: String): String = when {
        stored.isNotEmpty() -> stored
        SUPPORTED_LANGUAGES.contains(phoneLanguage) -> phoneLanguage
        else -> FALLBACK_LANGUAGE
    }

    companion object {
        /** FR-6.3 / FR-8.1, in the order the Settings list shows them. */
        val SUPPORTED_LANGUAGES = listOf("en", "bn", "hi", "ru", "es", "it")
        const val FALLBACK_LANGUAGE = "en"
    }
}
