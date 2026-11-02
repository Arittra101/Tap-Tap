package com.taptap.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taptap.data.repository.CounterRepository
import com.taptap.model.Counter
import com.taptap.model.CounterType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Edit counter (FR-5.1 - FR-5.8). */
class EditViewModel(
    private val counterId: Long,
    private val repository: CounterRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state.asStateFlow()

    private val _effects = Channel<EditEffect>(Channel.BUFFERED)
    val effects: Flow<EditEffect> = _effects.receiveAsFlow()

    /** The counter as it was loaded, so Save can send back the fields Edit does not own. */
    private var loaded: Counter? = null

    init {
        viewModelScope.launch {
            // Loaded once, not observed: a form that rewrites itself underneath the user
            // while they type would lose their edits.
            val counter = repository.observeCounter(counterId).first() ?: return@launch
            loaded = counter
            _state.update {
                EditState(
                    isLoading = false,
                    type = counter.type,
                    name = counter.name,
                    notes = counter.notes.orEmpty(),
                    color = counter.color,
                    background = counter.background,
                    isPinned = counter.isPinned,
                    startValue = counter.startValue,
                    step = counter.step,
                    targetOn = counter.target != null,
                    target = counter.target ?: 50,
                    keepCountingAfterTarget = counter.keepCountingAfterTarget,
                    quitAtMillis = counter.quitAt,
                    moneyPerDay = counter.moneyPerDay?.let { m ->
                        if (m % 1.0 == 0.0) m.toLong().toString() else m.toString()
                    }.orEmpty(),
                    goalOn = counter.goalDays != null,
                    goalDays = counter.goalDays ?: 30,
                    reason = counter.reason.orEmpty(),
                    badgesOn = counter.badgesOn,
                    dailyMessageOn = counter.dailyMessageOn,
                    isFocus = counter.isFocus,
                )
            }
        }
    }

    fun onIntent(intent: EditIntent) {
        when (intent) {
            is EditIntent.ChangeName -> _state.update { it.copy(name = intent.value) }
            is EditIntent.ChangeNotes -> _state.update { it.copy(notes = intent.value) }
            is EditIntent.ChangeColor -> _state.update { it.copy(color = intent.color) }
            is EditIntent.ChangeBackground -> _state.update { it.copy(background = intent.background) }
            is EditIntent.ChangeStartValue -> _state.update {
                it.copy(startValue = intent.value.coerceAtLeast(0))
            }
            is EditIntent.ChangeStep -> _state.update { it.copy(step = intent.value.coerceAtLeast(1)) }
            is EditIntent.ToggleTarget -> _state.update { it.copy(targetOn = intent.on) }
            is EditIntent.ChangeTarget -> _state.update { it.copy(target = intent.value.coerceAtLeast(1)) }
            is EditIntent.ToggleKeepCounting -> _state.update {
                it.copy(keepCountingAfterTarget = intent.on)
            }
            is EditIntent.ChangeQuitDate -> _state.update {
                it.copy(quitAtMillis = intent.millis.coerceAtMost(clock()))
            }
            is EditIntent.ChangeMoneyPerDay -> _state.update {
                it.copy(moneyPerDay = intent.value.filter { ch -> ch.isDigit() || ch == '.' })
            }
            is EditIntent.ToggleGoal -> _state.update { it.copy(goalOn = intent.on) }
            is EditIntent.ChangeGoalDays -> _state.update {
                it.copy(goalDays = intent.value.coerceAtLeast(1))
            }
            is EditIntent.ChangeReason -> _state.update { it.copy(reason = intent.value) }
            is EditIntent.ToggleBadges -> _state.update { it.copy(badgesOn = intent.on) }
            is EditIntent.ToggleDailyMessage -> _state.update { it.copy(dailyMessageOn = intent.on) }
            is EditIntent.ToggleFocus -> _state.update { it.copy(isFocus = intent.on) }
            is EditIntent.TogglePin -> _state.update { it.copy(isPinned = intent.on) }
            EditIntent.Save -> save()
            EditIntent.DeleteClicked -> _state.update { it.copy(showDeleteDialog = true) }
            EditIntent.DeleteDismissed -> _state.update { it.copy(showDeleteDialog = false) }
            EditIntent.DeleteConfirmed -> {
                _state.update { it.copy(showDeleteDialog = false) }
                viewModelScope.launch {
                    repository.delete(counterId)
                    _effects.send(EditEffect.Deleted)
                }
            }
        }
    }

    private fun save() {
        val s = _state.value
        val original = loaded ?: return
        if (!s.canSave) return

        viewModelScope.launch {
            repository.update(
                original.copy(
                    name = s.name.trim(),
                    notes = s.notes.trim().takeIf { it.isNotEmpty() && s.type == CounterType.NORMAL },
                    color = s.color,
                    background = s.background,
                    isPinned = s.isPinned,
                    isFocus = s.type == CounterType.QUIT && s.isFocus,
                    // FR-5.4: a new start value takes effect at the next reset, a new step
                    // at the next tap. The repository refuses to move `count`.
                    startValue = s.startValue,
                    step = s.step,
                    target = if (s.type == CounterType.NORMAL && s.targetOn) s.target else null,
                    keepCountingAfterTarget = s.type == CounterType.NORMAL && s.keepCountingAfterTarget,
                    quitAt = if (s.type == CounterType.QUIT) s.quitAtMillis else null,
                    moneyPerDay = s.moneyPerDay.toDoubleOrNull()
                        ?.takeIf { s.type == CounterType.QUIT && it > 0 },
                    goalDays = if (s.type == CounterType.QUIT && s.goalOn) s.goalDays else null,
                    reason = s.reason.trim().takeIf { it.isNotEmpty() && s.type == CounterType.QUIT },
                    badgesOn = s.badgesOn,
                    dailyMessageOn = s.dailyMessageOn,
                ),
            )
            _effects.send(EditEffect.Saved)
        }
    }
}
