package com.taptap.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.taptap.data.repository.SettingsRepository
import com.taptap.data.settings.SettingsStore
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /** Each test gets its own preferences file, so write order between tests cannot leak. */
    private fun repo(): SettingsRepository {
        val dataStore = PreferenceDataStoreFactory.create {
            tempFolder.newFile("settings-${'$'}{counter++}.preferences_pb").also { it.delete() }
        }
        return SettingsRepository(SettingsStore(dataStore))
    }

    private var counter = 0

    @Test
    fun `defaults match the requirements`() = runTest {
        val s = repo().settings.first()
        assertEquals(ThemeColor.PINK, s.themeColor)
        assertEquals(ThemeMode.SYSTEM, s.themeMode)
        assertEquals("", s.languageTag)
        assertTrue("vibration defaults on (FR-6.4)", s.vibration)
        assertFalse("sound defaults off (FR-6.4)", s.sound)
    }

    @Test
    fun `every setting round-trips`() = runTest {
        val r = repo()
        r.setThemeColor(ThemeColor.TEAL)
        r.setThemeMode(ThemeMode.DARK)
        r.setLanguageTag("bn")
        r.setVibration(false)
        r.setSound(true)

        val s = r.settings.first()
        assertEquals(ThemeColor.TEAL, s.themeColor)
        assertEquals(ThemeMode.DARK, s.themeMode)
        assertEquals("bn", s.languageTag)
        assertFalse(s.vibration)
        assertTrue(s.sound)
    }

    @Test
    fun `an unchosen language follows a supported phone language`() {
        val r = repo()
        assertEquals("hi", r.resolveLanguageTag(stored = "", phoneLanguage = "hi"))
    }

    @Test
    fun `an unchosen language falls back to English when the phone is unsupported`() {
        val r = repo()
        assertEquals("en", r.resolveLanguageTag(stored = "", phoneLanguage = "de"))
    }

    @Test
    fun `a chosen language wins over the phone`() {
        val r = repo()
        assertEquals("ru", r.resolveLanguageTag(stored = "ru", phoneLanguage = "en"))
    }

    @Test
    fun `the six languages are the ones FR-6_3 names`() {
        assertEquals(
            listOf("en", "bn", "hi", "ru", "es", "it"),
            SettingsRepository.SUPPORTED_LANGUAGES,
        )
    }
}
