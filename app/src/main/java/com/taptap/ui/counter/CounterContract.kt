package com.taptap.ui.counter

import com.taptap.model.Background
import com.taptap.model.ThemeColor

/** The normal counter screen (FR-3.1 - FR-3.9). Shape taken from tech spec 4.2. */
data class CounterState(
    val isLoading: Boolean = true,
    val name: String = "",
    val notes: String = "",
    val count: Long = 0,
    val startValue: Long = 0,
    val target: Long? = null,
    val step: Int = 1,
    val todayChange: Long = 0,
    val isLocked: Boolean = false,
    val isReversed: Boolean = false,
    val isTargetReached: Boolean = false,
    val canTap: Boolean = true,
    val keepCountingAfterTarget: Boolean = false,
    val showResetDialog: Boolean = false,
    val color: ThemeColor? = null,
    val background: Background = Background.NONE,
) {
    /** FR-3.2: left to target, and percent done. Null target means both show a dash. */
    val leftToTarget: Long? get() = target?.let { (it - count).coerceAtLeast(0) }
    val percentDone: Int? get() = target?.takeIf { it > 0 }?.let {
        ((count.toFloat() / it) * 100).toInt().coerceIn(0, 100)
    }
}

sealed interface CounterIntent {
    data object Tap : CounterIntent
    data object ToggleLock : CounterIntent
    data object ToggleReverse : CounterIntent
    data object ResetClicked : CounterIntent
    data object ResetConfirmed : CounterIntent
    data object ResetDismissed : CounterIntent
}

sealed interface CounterEffect {
    /** FR-3.8: vibrate and click on a tap that actually counted. */
    data object TapFeedback : CounterEffect

    /** FR-8.4: a slightly longer haptic when the target is reached. */
    data object TargetReachedFeedback : CounterEffect
}
