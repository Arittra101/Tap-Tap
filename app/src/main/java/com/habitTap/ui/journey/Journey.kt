package com.habitTap.ui.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitTap.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The guided journey: make your first normal counter, then count with it.
 *
 * Each step names the one thing the user is being asked to do, and the step advances when
 * they do it, so the journey cannot get ahead of them. The one exception is the title,
 * where only the user knows when they have finished typing: that step waits for Next in
 * the character's card. A step the user solves their own way — naming and creating in one
 * go, without pressing Next — still lands in the right place.
 */
enum class JourneyStep {
    /** On Home: open the New counter form for a normal counter. */
    CREATE_COUNTER,

    /** On the form: give the counter a name. */
    NAME_IT,

    /** On the form: press Create counter. */
    TAP_CREATE,

    /** Back on Home: add one with the card's + button. */
    FIRST_TAP,
}

/**
 * Where the user is in the journey, for as long as it runs.
 *
 * One instance for the whole app, because the journey crosses screens: a step is set on
 * Home and answered on the New counter form. It is held in a Koin singleton rather than a
 * ViewModel for the same reason — no one screen owns it.
 *
 * Every `on…` method is a fact about what the user just did, not an instruction to move
 * on, and each only moves the step it belongs to. So they are safe to call from anywhere,
 * in any order, more than once: a user who creates a counter while the journey is not
 * running, or who is on a different step, changes nothing.
 */
class JourneyController(private val settings: SettingsRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _step = MutableStateFlow<JourneyStep?>(null)

    /** The step being shown, or null when the journey is not running. */
    val step: StateFlow<JourneyStep?> = _step.asStateFlow()

    /**
     * Starts the journey unless it has already been finished or stopped.
     *
     * Called when the app reaches Home, which is after the intro slides. It does nothing
     * when a journey is already running, so an Activity recreation mid-journey leaves the
     * user on the step they were on rather than sending them back to the first.
     */
    suspend fun startIfNeeded() {
        if (_step.value != null) return
        if (!settings.isJourneyDone()) _step.value = JourneyStep.CREATE_COUNTER
    }

    /** The New counter form was opened for a normal counter. */
    fun onCreateFormOpened() = advance(from = JourneyStep.CREATE_COUNTER, to = JourneyStep.NAME_IT)

    /** The user pressed Next on the title step, having given the counter a name. */
    fun onNamed() = advance(from = JourneyStep.NAME_IT, to = JourneyStep.TAP_CREATE)

    /**
     * A counter was created.
     *
     * Accepted from either form step: someone who types a name and presses Create in the
     * same breath never sits on [JourneyStep.TAP_CREATE] long enough for it to be seen.
     */
    fun onCounterCreated() {
        if (_step.value == JourneyStep.NAME_IT || _step.value == JourneyStep.TAP_CREATE) {
            _step.value = JourneyStep.FIRST_TAP
        }
    }

    /** A counter was tapped up from Home — the last thing the journey teaches. */
    fun onCounterTapped() {
        if (_step.value == JourneyStep.FIRST_TAP) finish()
    }

    /** "Stop tutorial". The user has asked not to be taught, so this is final. */
    fun stop() = finish()

    private fun advance(from: JourneyStep, to: JourneyStep) {
        if (_step.value == from) _step.value = to
    }

    /**
     * Ends the journey for good.
     *
     * The step is cleared at once and the flag written in the background: the overlay
     * should disappear on the same frame as the tap, not when DataStore gets around to it.
     */
    private fun finish() {
        _step.value = null
        scope.launch { settings.setJourneyDone() }
    }
}

/**
 * The running journey, or null when there is none.
 *
 * A composition local rather than a parameter on every screen: only two screens take part,
 * the journey is an overlay on top of what they already draw, and previews of those
 * screens should show the screen rather than the journey. Null is the default, so a
 * preview and a test see no journey without having to say so.
 */
val LocalJourney = staticCompositionLocalOf<JourneyController?> { null }

/** The step to show on this screen, or null when the journey is not running. */
@Composable
fun currentJourneyStep(): JourneyStep? {
    val controller = LocalJourney.current ?: return null
    val step by controller.step.collectAsStateWithLifecycle()
    return step
}

/**
 * The thing a step points at.
 *
 * The anchor is handed to the element with [journeyAnchor] and read by the overlay, which
 * draws the ring and places the character from the element's own position. Nothing has to
 * know in advance where on the screen anything sits.
 */
class JourneyAnchor {
    /** In root coordinates, null until the element has been laid out. */
    var bounds: Rect? by mutableStateOf(null)
        internal set
}

@Composable
fun rememberJourneyAnchor(): JourneyAnchor = remember { JourneyAnchor() }

/** Marks this element as what the current step is pointing at. */
fun Modifier.journeyAnchor(anchor: JourneyAnchor): Modifier =
    onGloballyPositioned { anchor.bounds = it.boundsInRoot() }
