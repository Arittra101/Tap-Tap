package com.taptap.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taptap.R
import com.taptap.data.repository.SettingsRepository
import com.taptap.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Tap feedback (FR-8.4, FR-8.5).
 *
 * The point of a one-big-target counter is that you can use it without looking, so the
 * confirmation has to be felt and heard rather than seen.
 */
interface Feedback {
    /** About 10 ms of haptic, plus a soft click, following the Settings switches. */
    fun tap()

    /** A slightly longer haptic, for reaching a target or earning a badge. */
    fun success()
}

private val NoFeedback = object : Feedback {
    override fun tap() = Unit
    override fun success() = Unit
}

/**
 * Builds a [Feedback] bound to the current settings.
 *
 * Both switches are read live, so turning one off takes effect on the very next tap
 * without a restart.
 */
@Composable
fun rememberFeedback(settings: Flow<AppSettings>): Feedback {
    val context = LocalContext.current
    val current by settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val latest by rememberUpdatedState(current)

    val player = remember(context) { ClickPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }

    val vibrator = remember(context) { context.vibrator() }

    return remember(player, vibrator) {
        object : Feedback {
            override fun tap() {
                if (latest.vibration) vibrator?.buzz(TAP_MS)
                if (latest.sound) player.click()
            }

            override fun success() {
                if (latest.vibration) vibrator?.buzz(SUCCESS_MS)
            }
        }
    }
}

/** For previews and tests. */
@Composable
fun rememberNoFeedback(): Feedback = NoFeedback

private const val TAP_MS = 10L
private const val SUCCESS_MS = 40L

private fun Context.vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
}

private fun Vibrator.buzz(millis: Long) {
    if (!hasVibrator()) return
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrate(millis)
        }
    }
}

/**
 * One short click, loaded once.
 *
 * It plays on the system stream, so the phone's silent mode mutes it — FR-8.5 asks for
 * exactly that, and a counter that beeps through silent mode would be a bug report.
 */
private class ClickPlayer(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private var soundId: Int = 0
    private var loaded = false

    init {
        runCatching {
            soundId = pool.load(context, R.raw.click, 1)
            pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
        }
    }

    fun click() {
        if (!loaded) return
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_VIBRATE) return
        pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        runCatching { pool.release() }
    }
}
