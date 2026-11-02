package com.taptap.ui.home

import com.taptap.model.Counter

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
)

sealed interface HomeIntent {
    data class AddTap(val counterId: Long) : HomeIntent
    data class BeatUrge(val counterId: Long) : HomeIntent
    data class TogglePin(val counterId: Long) : HomeIntent
    data class MakeFocus(val counterId: Long) : HomeIntent
}

sealed interface HomeEffect {
    /** A tap landed: vibrate and click, per the Settings switches (FR-3.8). */
    data object TapFeedback : HomeEffect
}
