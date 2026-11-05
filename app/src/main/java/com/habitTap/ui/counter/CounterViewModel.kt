package com.habitTap.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitTap.data.repository.CounterRepository
import com.habitTap.util.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The normal counter screen (FR-3.1 - FR-3.9). */
@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModel(
    private val counterId: Long,
    private val repository: CounterRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    private val _effects = Channel<CounterEffect>(Channel.BUFFERED)
    val effects: Flow<CounterEffect> = _effects.receiveAsFlow()

    /** Only needed so "Today" rolls over at midnight while the screen is open. */
    private val dayTicker: Flow<Long> = flow {
        while (true) {
            emit(clock())
            delay(60_000)
        }
    }

    init {
        viewModelScope.launch {
            dayTicker
                .flatMapLatest { now ->
                    val since = TimeUtils.startOfToday(now)
                    combine(
                        repository.observeCounter(counterId),
                        repository.observeChangeSince(counterId, since),
                    ) { counter, change -> counter to change }
                }
                .collect { (counter, todayChange) ->
                    if (counter == null) return@collect
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = counter.name,
                            notes = counter.notes.orEmpty(),
                            count = counter.count,
                            startValue = counter.startValue,
                            target = counter.target,
                            step = counter.step,
                            todayChange = todayChange,
                            isLocked = counter.isLocked,
                            isReversed = counter.isReversed,
                            isTargetReached = counter.isTargetReached,
                            canTap = counter.canTap,
                            keepCountingAfterTarget = counter.keepCountingAfterTarget,
                            color = counter.color,
                            background = counter.background,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Tap -> tap()
            CounterIntent.ToggleLock -> viewModelScope.launch { repository.toggleLock(counterId) }
            CounterIntent.ToggleReverse -> viewModelScope.launch { repository.toggleReverse(counterId) }
            CounterIntent.ResetClicked -> _state.update { it.copy(showResetDialog = true) }
            CounterIntent.ResetDismissed -> _state.update { it.copy(showResetDialog = false) }
            CounterIntent.ResetConfirmed -> {
                _state.update { it.copy(showResetDialog = false) }
                viewModelScope.launch { repository.reset(counterId) }
            }
        }
    }

    private fun tap() {
        // The screen already hides the tap for locked / finished counters; this guard is
        // for a race where state has not caught up yet.
        if (!_state.value.canTap) return
        viewModelScope.launch {
            val wasReached = _state.value.isTargetReached
            if (repository.tap(counterId) == 0L) return@launch
            _effects.send(CounterEffect.TapFeedback)
            // FR-8.4: a longer haptic the moment the target is reached.
            val after = repository.getCounter(counterId)
            if (after != null && after.isTargetReached && !wasReached) {
                _effects.send(CounterEffect.TargetReachedFeedback)
            }
        }
    }
}
