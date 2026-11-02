package com.taptap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taptap.data.repository.SettingsRepository
import com.taptap.navigation.AppNavigation
import com.taptap.ui.theme.TapTapTheme
import org.koin.android.ext.android.inject

/**
 * The single activity.
 *
 * It extends [AppCompatActivity] rather than ComponentActivity because per-app locales
 * (FR-8.2) need AppCompat's backport below API 33.
 */
class MainActivity : AppCompatActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Seeded from the repository's cache, so a recreation (a system light/dark
            // switch causes one) never paints a frame with the default theme.
            val current by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = settingsRepository.current,
            )
            // FR-6.1 / FR-6.2: a theme change repaints at once, with no restart.
            TapTapTheme(color = current.themeColor, mode = current.themeMode) {
                AppNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
