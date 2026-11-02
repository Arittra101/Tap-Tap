package com.taptap.ui.edit

import com.taptap.model.Background
import com.taptap.model.CounterType
import com.taptap.model.ThemeColor

/** Edit counter (FR-5.1 - FR-5.8). The quit variant swaps start/step/target for the quit fields. */
data class EditState(
    val isLoading: Boolean = true,
    val type: CounterType = CounterType.NORMAL,
    val name: String = "",
    val notes: String = "",
    val color: ThemeColor? = null,
    val background: Background = Background.NONE,
    val isPinned: Boolean = false,
    // Normal
    val startValue: Long = 0,
    val step: Int = 1,
    val targetOn: Boolean = false,
    val target: Long = 50,
    val keepCountingAfterTarget: Boolean = false,
    // Quit
    val quitAtMillis: Long? = null,
    val moneyPerDay: String = "",
    val goalOn: Boolean = false,
    val goalDays: Int = 30,
    val reason: String = "",
    val badgesOn: Boolean = true,
    val dailyMessageOn: Boolean = true,
    val isFocus: Boolean = false,
    // Screen-only
    val showDeleteDialog: Boolean = false,
) {
    val isQuit: Boolean get() = type == CounterType.QUIT

    val canSave: Boolean get() = name.isNotBlank()

    /**
     * TT-031 decision 1: a start value above the target is allowed, but the user is told
     * that a reset will already be past it.
     */
    val startAboveTarget: Boolean get() = targetOn && startValue > target
}

sealed interface EditIntent {
    data class ChangeName(val value: String) : EditIntent
    data class ChangeNotes(val value: String) : EditIntent
    data class ChangeColor(val color: ThemeColor?) : EditIntent
    data class ChangeBackground(val background: Background) : EditIntent
    data class ChangeStartValue(val value: Long) : EditIntent
    data class ChangeStep(val value: Int) : EditIntent
    data class ToggleTarget(val on: Boolean) : EditIntent
    data class ChangeTarget(val value: Long) : EditIntent
    data class ToggleKeepCounting(val on: Boolean) : EditIntent
    data class ChangeQuitDate(val millis: Long) : EditIntent
    data class ChangeMoneyPerDay(val value: String) : EditIntent
    data class ToggleGoal(val on: Boolean) : EditIntent
    data class ChangeGoalDays(val value: Int) : EditIntent
    data class ChangeReason(val value: String) : EditIntent
    data class ToggleBadges(val on: Boolean) : EditIntent
    data class ToggleDailyMessage(val on: Boolean) : EditIntent
    data class ToggleFocus(val on: Boolean) : EditIntent
    data class TogglePin(val on: Boolean) : EditIntent
    data object Save : EditIntent
    data object DeleteClicked : EditIntent
    data object DeleteConfirmed : EditIntent
    data object DeleteDismissed : EditIntent
}

sealed interface EditEffect {
    data object Saved : EditEffect
    data object Deleted : EditEffect
}
