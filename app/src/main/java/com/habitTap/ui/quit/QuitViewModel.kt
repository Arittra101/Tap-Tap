package com.habitTap.ui.quit

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

/** The quit counter screen (FR-4.1 - FR-4.8). */
@OptIn(ExperimentalCoroutinesApi::class)
class QuitViewModel(
    private val counterId: Long,
    private val repository: CounterRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(QuitState())
    val state: StateFlow<QuitState> = _state.asStateFlow()

    private val _effects = Channel<QuitEffect>(Channel.BUFFERED)
    val effects: Flow<QuitEffect> = _effects.receiveAsFlow()

    /** Days and hours have to keep moving while the screen is open (tech spec 8). */
    private val minuteTicker: Flow<Long> = flow {
        while (true) {
            emit(clock())
            delay(60_000)
        }
    }

    init {
        viewModelScope.launch {
            minuteTicker
                .flatMapLatest { now ->
                    val since = TimeUtils.startOfToday(now)
                    combine(
                        repository.observeCounter(counterId),
                        repository.observeUrgesSince(counterId, since),
                        repository.observeUrgesTotal(counterId),
                    ) { counter, today, total -> Triple(counter, today, total) }
                        .combine(flow { emit(now) }) { triple, n -> triple to n }
                }
                .collect { (triple, now) ->
                    val (counter, urgesToday, urgesTotal) = triple
                    if (counter == null) return@collect

                    val quitAt = counter.quitAt ?: now
                    val days = TimeUtils.daysFree(quitAt, now)
                    val hours = TimeUtils.hoursFree(quitAt, now)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = counter.name,
                            quitAt = quitAt,
                            daysFree = days,
                            hoursFree = hours,
                            nextMilestone = TimeUtils.nextMilestone(days),
                            milestoneProgress = TimeUtils.milestoneProgress(days, hours),
                            earnedBadges = TimeUtils.earnedBadgeCount(days),
                            moneySaved = TimeUtils.moneySaved(days, counter.moneyPerDay),
                            urgesToday = urgesToday,
                            urgesTotal = urgesTotal,
                            // FR-4.3: the current run counts if it already beats the record.
                            bestStreakDays = maxOf(counter.bestStreakDays, days),
                            reason = counter.reason,
                            badgesOn = counter.badgesOn,
                            isFocus = counter.isFocus,
                            isPinned = counter.isPinned,
                            color = counter.color,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: QuitIntent) {
        when (intent) {
            QuitIntent.BeatUrge -> viewModelScope.launch {
                repository.beatUrge(counterId)
                _effects.send(QuitEffect.UrgeFeedback)
            }
            QuitIntent.ToggleFocus -> viewModelScope.launch {
                if (_state.value.isFocus) repository.clearFocus() else repository.setFocus(counterId)
            }
            QuitIntent.TogglePin -> viewModelScope.launch { repository.togglePin(counterId) }
            QuitIntent.SlipClicked -> _state.update { it.copy(showSlipDialog = true) }
            QuitIntent.SlipDismissed -> _state.update { it.copy(showSlipDialog = false) }
            QuitIntent.SlipConfirmed -> {
                _state.update { it.copy(showSlipDialog = false) }
                viewModelScope.launch { repository.slip(counterId) }
            }
        }
    }
}
