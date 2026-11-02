package com.taptap.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.taptap.model.AppSettings
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * The five stored preferences (requirements 6, tech spec 7.4).
 *
 * The [DataStore] is injected rather than reached for through a Context delegate: the
 * delegate caches one instance per process, which makes the store impossible to isolate
 * in a test. [create] builds the real one.
 *
 * A value that no longer parses — an enum renamed in a later version, say — falls back to
 * its default instead of throwing, so a downgrade cannot leave the app unable to start.
 */
class SettingsStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val themeColor = stringPreferencesKey("theme_color")
        val themeMode = stringPreferencesKey("theme_mode")
        val language = stringPreferencesKey("language")
        val vibration = booleanPreferencesKey("vibration")
        val sound = booleanPreferencesKey("sound")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { cause ->
            // A corrupt preferences file should not take the app down with it.
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { prefs ->
            AppSettings(
                themeColor = prefs[Keys.themeColor].toThemeColor(),
                themeMode = prefs[Keys.themeMode].toThemeMode(),
                languageTag = prefs[Keys.language].orEmpty(),
                vibration = prefs[Keys.vibration] ?: DEFAULT_VIBRATION,
                sound = prefs[Keys.sound] ?: DEFAULT_SOUND,
            )
        }

    suspend fun setThemeColor(value: ThemeColor) {
        dataStore.edit { it[Keys.themeColor] = value.name }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        dataStore.edit { it[Keys.themeMode] = value.name }
    }

    /** An empty tag means "follow the phone" (requirements 6). */
    suspend fun setLanguageTag(tag: String) {
        dataStore.edit { it[Keys.language] = tag }
    }

    suspend fun setVibration(enabled: Boolean) {
        dataStore.edit { it[Keys.vibration] = enabled }
    }

    suspend fun setSound(enabled: Boolean) {
        dataStore.edit { it[Keys.sound] = enabled }
    }

    private fun String?.toThemeColor(): ThemeColor =
        enumValues<ThemeColor>().firstOrNull { it.name == this } ?: DEFAULT_COLOR

    private fun String?.toThemeMode(): ThemeMode =
        enumValues<ThemeMode>().firstOrNull { it.name == this } ?: DEFAULT_MODE

    companion object {
        const val FILE_NAME = "settings"

        val DEFAULT_COLOR = ThemeColor.PINK
        val DEFAULT_MODE = ThemeMode.SYSTEM
        const val DEFAULT_VIBRATION = true
        const val DEFAULT_SOUND = false

        /** The app's real preferences file. Wired up once, in the Koin module. */
        fun create(context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(FILE_NAME)
            }
    }
}
