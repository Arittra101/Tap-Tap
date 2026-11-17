package com.habitTap

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.navigation.AppNavigation
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.tutorial.TutorialScreen
import com.habitTap.util.LocalCurrencyCode
import kotlin.system.exitProcess
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

    /**
     * Null until the stored settings have been read.
     *
     * Two things wait on it: the splash screen stays up while it is null, and the first
     * screen is chosen from it — the tutorial for someone who has not seen it, Home for
     * everyone else. The stored default is "not seen", so deciding before the read would
     * show the tutorial to everyone on every launch.
     */
    private var tutorialSeen by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate, as the splash screen API requires. It swaps the launch
        // theme for the app's own, so Theme.TapTap.Splash is only ever the window the
        // system paints before this line runs.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The splash is held until the settings are in hand. That read also fills the
        // repository's cache, so the first frame after it is already the user's own
        // colour and currency: the splash covers the read instead of the app flashing
        // through it.
        splash.setKeepOnScreenCondition { tutorialSeen == null }
        lifecycleScope.launch { tutorialSeen = settingsRepository.awaitLoaded().tutorialSeen }

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
                when (tutorialSeen) {
                    // The splash is still up, so nothing here is on screen yet.
                    null -> Unit
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
                    else -> AppNavigation(
                        modifier = Modifier.fillMaxSize(),
                        // Asked for explicitly: leaving ends the app rather than
                        // backgrounding it. finishAffinity() first, so nothing is left
                        // to be restored, and only then the process.
                        onExit = {
                            finishAffinity()
                            exitProcess(0)
                        },
                    )
                }
                }
            }
        }
    }
}
