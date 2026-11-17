package com.habitTap.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationAttributes
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
import com.habitTap.R
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.model.AppSettings
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
                if (latest.vibration) vibrator?.buzz(Buzz.TAP)
                if (latest.sound) player.click()
            }

            override fun success() {
                if (latest.vibration) vibrator?.buzz(Buzz.SUCCESS)
            }
        }
    }
}

/** For previews and tests. */
@Composable
fun rememberNoFeedback(): Feedback = NoFeedback

private enum class Buzz { TAP, SUCCESS }

/**
 * Fallback durations for devices too old for the predefined effects.
 *
 * 10 ms — the first value here — turned out to be unfeelable: it is shorter than the
 * rise time of the linear actuators in current phones, so the motor never reaches a
 * perceptible amplitude before it is told to stop.
 */
private const val TAP_FALLBACK_MS = 22L
private const val SUCCESS_FALLBACK_MS = 60L

/**
 * The buzz is the app's own, not the system's touch feedback.
 *
 * `USAGE_ASSISTANCE_SONIFICATION` maps to `VibrationAttributes.USAGE_TOUCH`, and the
 * system — aggressively so on Xiaomi, Oppo, Realme and Vivo ROMs — gates that behind the
 * phone's own "touch feedback" switch. The vibration here is not touch feedback for a
 * system widget; it is the thing the user asked for with the switch in this app's
 * Settings, so it carries no system usage and that gate does not apply to it.
 *
 * Do Not Disturb is still respected: suppressing a buzz during DND is what DND is for,
 * and bypassing it needs a permission this app has no business holding.
 */
private val AppVibrationAttributes: AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_UNKNOWN)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()

/**
 * Plays [effect] with the app's own usage.
 *
 * API 33 took the `VibrationAttributes` overload public, and it says the same thing more
 * directly than the audio attributes it replaces.
 */
private fun Vibrator.play(effect: VibrationEffect) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        vibrate(
            effect,
            VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_UNKNOWN)
                .build(),
        )
    } else {
        vibrate(effect, AppVibrationAttributes)
    }
}

private fun Context.vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
} else {
    @Suppress("DEPRECATION")
    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
}

/**
 * Prefers the platform's predefined effects over a raw one-shot, but only where the
 * device actually implements them.
 *
 * `EFFECT_CLICK` and `EFFECT_HEAVY_CLICK` are tuned per device by the manufacturer, so
 * where they exist they are felt on hardware a hand-rolled duration is not, and they are
 * what the system itself uses for touch confirmation. Where they do **not** exist the
 * platform is free to do nothing at all, silently — which is how vibration came to work
 * on some phones and not others with the same code. So the support is asked for first,
 * and anything but a clear yes falls back to a plain one-shot, which every vibrator can
 * play.
 */
private fun Vibrator.buzz(kind: Buzz) {
    if (!hasVibrator()) return
    val millis = when (kind) {
        Buzz.TAP -> TAP_FALLBACK_MS
        Buzz.SUCCESS -> SUCCESS_FALLBACK_MS
    }
    runCatching {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && supportsPredefined(kind) -> {
                play(
                    VibrationEffect.createPredefined(
                        when (kind) {
                            Buzz.TAP -> VibrationEffect.EFFECT_CLICK
                            Buzz.SUCCESS -> VibrationEffect.EFFECT_HEAVY_CLICK
                        },
                    ),
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                play(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))

            else -> {
                @Suppress("DEPRECATION")
                vibrate(millis)
            }
        }
    }
}

/**
 * Whether this device plays the predefined effect for [kind].
 *
 * Only API 30 and up can answer. Below that the question cannot be asked, so the answer
 * is taken as yes — API 29 is the one version where the effects exist but their support
 * is unknowable, and a predefined effect is still the better bet there.
 *
 * `VIBRATION_EFFECT_SUPPORT_UNKNOWN` counts as no: it means the platform cannot promise
 * the effect does anything, and a one-shot that is definitely felt beats a tuned effect
 * that might be silent.
 */
private fun Vibrator.supportsPredefined(kind: Buzz): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return true
    val effect = when (kind) {
        Buzz.TAP -> VibrationEffect.EFFECT_CLICK
        Buzz.SUCCESS -> VibrationEffect.EFFECT_HEAVY_CLICK
    }
    return runCatching {
        areEffectsSupported(effect).firstOrNull() == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
    }.getOrDefault(false)
}

/**
 * One short click, loaded once.
 *
 * It plays on the media stream rather than the system one, which is what makes the switch
 * in this app's Settings mean what it says: the ringer's silent and vibrate modes govern
 * the ring and notification streams, not media, so a user who asked for a click gets one
 * whatever the ringer is doing. The sound switch is off by default and one tap from off
 * again, which is what keeps that honest.
 *
 * No audio focus is requested: a click this short should mix over whatever is playing,
 * not duck it.
 */
private class ClickPlayer(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private var soundId: Int = 0

    /**
     * `@Volatile` because which thread sets this depends on where SoundPool's listener
     * was registered, and a stale `false` read here means permanent silence.
     */
    @Volatile
    private var loaded = false

    init {
        runCatching {
            // The listener goes on BEFORE the load, and this order is the whole fix.
            //
            // SoundPool only builds its event handler when a listener is registered, and
            // its native loader drops the completion event outright if no handler exists
            // yet. Loading runs on SoundPool's own thread, so a short wav off local
            // storage can finish before a listener registered on the line after load()
            // exists — the event is then thrown away, `loaded` stays false for the life
            // of the process, and the app is silent. Whether that happened came down to
            // how fast the device finished the load, which is why the click worked on
            // some phones and not others from identical code.
            //
            // Any successful load is ours: this pool holds one sample. The id is
            // deliberately not compared, because it is assigned by the line below and
            // would still be 0 if the callback ever did arrive first.
            pool.setOnLoadCompleteListener { _, _, status -> if (status == 0) loaded = true }
            soundId = pool.load(context, R.raw.click, 1)
        }
    }

    fun click() {
        if (!loaded) return
        pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        runCatching { pool.release() }
    }
}
