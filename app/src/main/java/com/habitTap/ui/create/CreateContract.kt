package com.habitTap.ui.create

import com.habitTap.model.CounterType

/** The New counter form (FR-2.1 - FR-2.7). The type decides which half is shown. */
data class CreateState(
    val type: CounterType = CounterType.NORMAL,
    val name: String = "",
    val notes: String = "",
    // Normal
    val startValue: Long = 0,
    val step: Int = 1,
    val targetOn: Boolean = false,
    val target: Long = 50,
    val keepCountingAfterTarget: Boolean = false,
    // Quit
    val habitPreset: String? = null,
    val quitToday: Boolean = true,
    val quitDateMillis: Long? = null,
    val moneyPerDay: String = "",
    val goalOn: Boolean = true,
    val goalDays: Int = 30,
    val reason: String = "",
    val badgesOn: Boolean = true,
    val dailyMessageOn: Boolean = true,
    val makeFocus: Boolean = false,
    // Both
    val isPinned: Boolean = false,
    val isSaving: Boolean = false,
    /** The leave-form sheet: open after [CreateIntent.Back] on a named normal counter. */
    val showExitSheet: Boolean = false,
) {
    val isQuit: Boolean get() = type == CounterType.QUIT

    /** FR-2.5: the create button stays disabled until the name has text. */
    val canCreate: Boolean get() = name.isNotBlank() && !isSaving

    /**
     * A normal counter needs a title, so a title already typed is work worth asking
     * about before it is thrown away. A quit counter fills its name from the preset the
     * moment the type is picked, which is not something the user wrote, so leaving it
     * goes straight back.
     */
    val shouldAskBeforeLeaving: Boolean
        get() = type == CounterType.NORMAL && name.isNotBlank() && !isSaving
}

sealed interface CreateIntent {
    data class ChangeType(val type: CounterType) : CreateIntent
    data class ChangeName(val value: String) : CreateIntent
    data class ChangeNotes(val value: String) : CreateIntent
    /** The screen resolves [name] from resources, so the ViewModel stays Android-free. */
    data class PickHabitPreset(val id: String, val name: String) : CreateIntent
    data class ChangeStartValue(val value: Long) : CreateIntent
    data class ChangeStep(val value: Int) : CreateIntent
    data class ToggleTarget(val on: Boolean) : CreateIntent
    data class ChangeTarget(val value: Long) : CreateIntent
    data class ToggleKeepCounting(val on: Boolean) : CreateIntent
    data class ChangeQuitToday(val today: Boolean) : CreateIntent
    data class ChangeQuitDate(val millis: Long) : CreateIntent
    data class ChangeMoneyPerDay(val value: String) : CreateIntent
    data class ToggleGoal(val on: Boolean) : CreateIntent
    data class ChangeGoalDays(val value: Int) : CreateIntent
    data class ChangeReason(val value: String) : CreateIntent
    data class ToggleBadges(val on: Boolean) : CreateIntent
    data class ToggleDailyMessage(val on: Boolean) : CreateIntent
    data class ToggleMakeFocus(val on: Boolean) : CreateIntent
    data class TogglePin(val on: Boolean) : CreateIntent
    data object Create : CreateIntent

    /** The top bar arrow or the system back. May open the sheet instead of leaving. */
    data object Back : CreateIntent

    /** Closing the sheet without deciding: the scrim, a swipe down, or back again. */
    data object ExitSheetDismissed : CreateIntent

    /** "Discard" in the sheet: throw the form away and leave. */
    data object Discard : CreateIntent
}

sealed interface CreateEffect {
    /** FR-2.7: created, so go back to Home where the counter now appears in its group. */
    data class Created(val counterId: Long) : CreateEffect

    /** Leave without creating anything: nothing was written, or "Discard" was tapped. */
    data object Leave : CreateEffect
}
