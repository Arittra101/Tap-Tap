package com.taptap.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taptap.data.repository.CounterRepository
import com.taptap.model.Background
import com.taptap.model.Counter
import com.taptap.model.CounterType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** New counter (FR-2.1 - FR-2.7). */
class CreateViewModel(
    private val repository: CounterRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(CreateState())
    val state: StateFlow<CreateState> = _state.asStateFlow()

    private val _effects = Channel<CreateEffect>(Channel.BUFFERED)
    val effects: Flow<CreateEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: CreateIntent) {
        when (intent) {
            is CreateIntent.ChangeType -> changeType(intent.type)
            is CreateIntent.ChangeName -> _state.update {
                // Typing a name of your own means no preset is selected any more.
                it.copy(name = intent.value, habitPreset = null)
            }
            is CreateIntent.ChangeNotes -> _state.update { it.copy(notes = intent.value) }
            is CreateIntent.PickHabitPreset -> _state.update {
                it.copy(habitPreset = intent.id, name = intent.name)
            }
            is CreateIntent.ChangeStartValue -> _state.update {
                it.copy(startValue = intent.value.coerceAtLeast(0))
            }
            is CreateIntent.ChangeStep -> _state.update {
                it.copy(step = intent.value.coerceAtLeast(1))
            }
            is CreateIntent.ToggleTarget -> _state.update { it.copy(targetOn = intent.on) }
            is CreateIntent.ChangeTarget -> _state.update {
                it.copy(target = intent.value.coerceAtLeast(1))
            }
            is CreateIntent.ToggleKeepCounting -> _state.update {
                it.copy(keepCountingAfterTarget = intent.on)
            }
            is CreateIntent.ChangeQuitToday -> _state.update { it.copy(quitToday = intent.today) }
            is CreateIntent.ChangeQuitDate -> _state.update {
                // FR-2.3: today or an earlier date, never the future.
                it.copy(quitDateMillis = intent.millis.coerceAtMost(clock()), quitToday = false)
            }
            is CreateIntent.ChangeMoneyPerDay -> _state.update {
                it.copy(moneyPerDay = intent.value.filter { ch -> ch.isDigit() || ch == '.' })
            }
            is CreateIntent.ToggleGoal -> _state.update { it.copy(goalOn = intent.on) }
            is CreateIntent.ChangeGoalDays -> _state.update {
                it.copy(goalDays = intent.value.coerceAtLeast(1))
            }
            is CreateIntent.ChangeReason -> _state.update { it.copy(reason = intent.value) }
            is CreateIntent.ToggleBadges -> _state.update { it.copy(badgesOn = intent.on) }
            is CreateIntent.ToggleDailyMessage -> _state.update { it.copy(dailyMessageOn = intent.on) }
            is CreateIntent.ToggleMakeFocus -> _state.update { it.copy(makeFocus = intent.on) }
            is CreateIntent.TogglePin -> _state.update { it.copy(isPinned = intent.on) }
            CreateIntent.Create -> create()
        }
    }

    /**
     * Switching type starts the other form clean.
     *
     * A name typed for a normal counter rarely makes sense as a habit to quit, and the
     * design canvas does the same: picking Quit fills "Quit cigarettes", picking Normal
     * clears the field.
     */
    private fun changeType(type: CounterType) = _state.update {
        if (it.type == type) return@update it
        when (type) {
            // The screen fills the preset's name on first composition for this type.
            CounterType.QUIT -> it.copy(type = type, habitPreset = null, name = "")
            CounterType.NORMAL -> it.copy(type = type, habitPreset = null, name = "")
        }
    }

    private fun create() {
        val s = _state.value
        if (!s.canCreate) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = clock()
            val counter = Counter(
                id = 0,
                type = s.type,
                name = s.name.trim(),
                notes = s.notes.trim().takeIf { it.isNotEmpty() && s.type == CounterType.NORMAL },
                color = null,
                background = Background.NONE,
                isPinned = s.isPinned,
                isFocus = s.type == CounterType.QUIT && s.makeFocus,
                createdAt = now,
                sortOrder = 0,
                count = if (s.type == CounterType.NORMAL) s.startValue else 0,
                startValue = if (s.type == CounterType.NORMAL) s.startValue else 0,
                step = if (s.type == CounterType.NORMAL) s.step else 1,
                target = if (s.type == CounterType.NORMAL && s.targetOn) s.target else null,
                keepCountingAfterTarget = s.type == CounterType.NORMAL && s.keepCountingAfterTarget,
                isLocked = false,
                isReversed = false,
                quitAt = if (s.type == CounterType.QUIT) {
                    if (s.quitToday) now else (s.quitDateMillis ?: now)
                } else {
                    null
                },
                moneyPerDay = s.moneyPerDay.toDoubleOrNull()?.takeIf { s.type == CounterType.QUIT && it > 0 },
                goalDays = if (s.type == CounterType.QUIT && s.goalOn) s.goalDays else null,
                reason = s.reason.trim().takeIf { it.isNotEmpty() && s.type == CounterType.QUIT },
                bestStreakDays = 0,
                badgesOn = s.type != CounterType.QUIT || s.badgesOn,
                dailyMessageOn = s.type != CounterType.QUIT || s.dailyMessageOn,
                dailyMessageMinute = 9 * 60,
            )
            val id = repository.create(counter)
            _effects.send(CreateEffect.Created(id))
        }
    }
}

/** The five habit presets and the name each one writes (FR-2.3). */
object HabitPreset {
    const val CIGARETTES = "cigarettes"
    const val ALCOHOL = "alcohol"
    const val SOFT_DRINKS = "soft_drinks"
    const val PHONE_NIGHT = "phone_night"
    const val OTHER = "other"

    val all = listOf(CIGARETTES, ALCOHOL, SOFT_DRINKS, PHONE_NIGHT, OTHER)

    /**
     * The name a preset fills in, as a resource id. "Other" fills nothing — the user
     * writes their own.
     */
    fun nameResFor(id: String): Int? = when (id) {
        CIGARETTES -> com.taptap.R.string.habit_name_cigarettes
        ALCOHOL -> com.taptap.R.string.habit_name_alcohol
        SOFT_DRINKS -> com.taptap.R.string.habit_name_soft_drinks
        PHONE_NIGHT -> com.taptap.R.string.habit_name_phone_night
        else -> null
    }

    fun labelResFor(id: String): Int = when (id) {
        CIGARETTES -> com.taptap.R.string.habit_cigarettes
        ALCOHOL -> com.taptap.R.string.habit_alcohol
        SOFT_DRINKS -> com.taptap.R.string.habit_soft_drinks
        PHONE_NIGHT -> com.taptap.R.string.habit_phone_night
        else -> com.taptap.R.string.habit_other
    }
}
