package com.habitTap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.navigation.AppNavigation
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.tutorial.TutorialScreen
import com.habitTap.util.LocalCurrencyCode
import kotlinx.coroutines.launch
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
                // Every screen that draws money reads this rather than taking a parameter.
                CompositionLocalProvider(LocalCurrencyCode provides current.currencyCode) {
                // null until the stored flag has been read. The flag's default is "not
                // seen", so showing the tutorial while the answer is unknown would flash
                // it at everyone on every launch; an empty background for the one frame
                // the read takes shows it only to the people it is for.
                var tutorialSeen by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) { tutorialSeen = settingsRepository.isTutorialSeen() }

                when (tutorialSeen) {
                    null -> Box(Modifier.fillMaxSize().background(TapTapTheme.colors.bg))
                    false -> TutorialScreen(
                        onDone = {
                            // Switched here and written in the background: Home should
                            // not wait on DataStore, and the write outliving this
                            // composition is the point, so it goes on the lifecycle scope.
                            tutorialSeen = true
                            lifecycleScope.launch { settingsRepository.setTutorialSeen() }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> AppNavigation(modifier = Modifier.fillMaxSize())
                }
                }
            }
        }
    }
}
