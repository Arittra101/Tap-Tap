package com.taptap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taptap.data.repository.CounterRepository
import com.taptap.data.repository.SettingsRepository
import com.taptap.model.CounterType
import com.taptap.util.TimeUtils
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

/**
 * Home (FR-1.1 - FR-1.11).
 *
 * The lists are derived here rather than in the screen so the ordering rule lives in one
 * place: the DAO already returns pinned-first, so splitting by type preserves it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: CounterRepository,
    @Suppress("unused") private val settingsRepository: SettingsRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    /**
     * Days free must stay current while Home is open, without the user doing anything
     * (tech spec 8). A minute is fine: the smallest unit on screen is an hour.
     */
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
                        repository.observeAll(),
                        repository.observeUrgesTodayAll(since),
                    ) { counters, urges -> Triple(counters, urges, now) }
                }
                .collect { (counters, urges, now) ->
                    val focus = counters.firstOrNull { it.type == CounterType.QUIT && it.isFocus }
                    val quits = counters.filter { it.type == CounterType.QUIT }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            focus = focus,
                            focusUrgesToday = focus?.let { f -> urges[f.id] } ?: 0,
                            normalCounters = counters.filter { c -> c.type == CounterType.NORMAL },
                            // FR-1.7: the quit list excludes whichever one is the focus.
                            quitCounters = quits.filter { q -> q.id != focus?.id },
                            quitUrgesToday = urges,
                            isEmpty = counters.isEmpty(),
                            // FR-1.3 / FR-1.4: the picker only appears when there are quit
                            // counters but no focus; with none at all the area is hidden.
                            showFocusPicker = focus == null && quits.isNotEmpty(),
                            allQuitCounters = quits,
                            nowMillis = now,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.AddTap -> viewModelScope.launch {
                // Home's + goes through the same repository call as the dial, so the two
                // can never disagree (tech spec 4.3).
                if (repository.tap(intent.counterId) != 0L) _effects.send(HomeEffect.TapFeedback)
            }
            is HomeIntent.BeatUrge -> viewModelScope.launch {
                repository.beatUrge(intent.counterId)
                _effects.send(HomeEffect.TapFeedback)
            }
            is HomeIntent.TogglePin -> viewModelScope.launch { repository.togglePin(intent.counterId) }
            is HomeIntent.MakeFocus -> viewModelScope.launch { repository.setFocus(intent.counterId) }
        }
    }
}
