package com.habitTap.data.repository

import com.habitTap.data.settings.SettingsStore
import com.habitTap.model.AppSettings
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * The app's settings, as one stream plus one setter per key (tech spec 7.4).
 *
 * [SUPPORTED_LANGUAGES] is the list from FR-6.3, in the order Settings shows them.
 */
class SettingsRepository(private val store: SettingsStore) {

    /**
     * Application-scoped: this class is a singleton, so the latest settings stay cached
     * across an Activity recreation (a system light/dark switch causes one).
     *
     * Without the cache the first frame after recreation used [AppSettings] defaults —
     * Pink — which visibly overrode whatever colour the user had chosen.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings: StateFlow<AppSettings> = store.settings
        .stateIn(scope, SharingStarted.Eagerly, AppSettings())

    /** The latest known settings, for seeding a composition without a default frame. */
    val current: AppSettings get() = settings.value

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
