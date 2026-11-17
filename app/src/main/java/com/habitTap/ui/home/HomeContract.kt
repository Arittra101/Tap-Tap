package com.habitTap.ui.home

import com.habitTap.model.Counter

/** Everything Home draws (FR-1.1 - FR-1.11). Defaults make the first frame safe. */
data class HomeState(
    val isLoading: Boolean = true,
    val focus: Counter? = null,
    val focusUrgesToday: Int = 0,
    val normalCounters: List<Counter> = emptyList(),
    val quitCounters: List<Counter> = emptyList(),
    val quitUrgesToday: Map<Long, Int> = emptyMap(),
    /** Every quit counter, focus included — the picker needs the full list (FR-1.3). */
    val allQuitCounters: List<Counter> = emptyList(),
    /** No counters at all: the first-run welcome, and the floating button is hidden. */
    val isEmpty: Boolean = false,
    /** Quit counters exist but none is the focus: show the picker (FR-1.3). */
    val showFocusPicker: Boolean = false,
    val nowMillis: Long = 0L,
    /** Screen-only: back was pressed and the app is asking before it closes. */
    val showExitSheet: Boolean = false,
)

sealed interface HomeIntent {
    /** The system back, on the one screen where it would otherwise close the app. */
    data object BackPressed : HomeIntent
    data object ExitDismissed : HomeIntent
    data object ExitConfirmed : HomeIntent
    data class AddTap(val counterId: Long) : HomeIntent
    data class BeatUrge(val counterId: Long) : HomeIntent
    data class TogglePin(val counterId: Long) : HomeIntent
    data class MakeFocus(val counterId: Long) : HomeIntent
}

sealed interface HomeEffect {
    /** A tap landed: vibrate and click, per the Settings switches (FR-3.8). */
    data object TapFeedback : HomeEffect

    /**
     * A new quit counter has appeared at the bottom of the page, so show the end.
     *
     * Quit counters live below the normal ones, far enough down that a new one can land
     * off screen entirely. Which of the three scrolls is sent depends on where the new
     * counter actually went, not on what the user ticked: pinning and focus both move it
     * somewhere else on the page.
     */
    data object ScrollToEnd : HomeEffect

    /** A new quit counter was pinned, so it is the first card of the quit list. */
    data object ScrollToQuitTop : HomeEffect

    /** A new quit counter was made the focus, which has its own card at the top of Home. */
    data object ScrollToTop : HomeEffect

    /** The user confirmed they are leaving. */
    data object Exit : HomeEffect
}
