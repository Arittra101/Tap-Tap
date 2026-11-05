package com.habitTap.ui.quit

import com.habitTap.model.ThemeColor

/** The quit counter screen (FR-4.1 - FR-4.8). */
data class QuitState(
    val isLoading: Boolean = true,
    val name: String = "",
    val quitAt: Long = 0L,
    val daysFree: Int = 0,
    val hoursFree: Int = 0,
    val nextMilestone: Int? = null,
    val milestoneProgress: Float = 0f,
    val earnedBadges: Int = 0,
    val moneySaved: Double? = null,
    val urgesToday: Int = 0,
    val urgesTotal: Int = 0,
    val bestStreakDays: Int = 0,
    val reason: String? = null,
    val badgesOn: Boolean = true,
    val isFocus: Boolean = false,
    val isPinned: Boolean = false,
    val color: ThemeColor? = null,
    val showSlipDialog: Boolean = false,
)

sealed interface QuitIntent {
    data object BeatUrge : QuitIntent
    data object ToggleFocus : QuitIntent
    data object TogglePin : QuitIntent
    data object SlipClicked : QuitIntent
    data object SlipConfirmed : QuitIntent
    data object SlipDismissed : QuitIntent
}

sealed interface QuitEffect {
    data object UrgeFeedback : QuitEffect
}
