package com.taptap.ui.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taptap.R
import com.taptap.model.Background
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import com.taptap.ui.components.ControlBar
import com.taptap.ui.components.ConfirmSheet
import com.taptap.ui.components.CounterBackground
import com.taptap.ui.components.CounterDial
import com.taptap.ui.components.DialState
import com.taptap.ui.components.IconButtonStyle
import com.taptap.ui.components.Stat
import com.taptap.ui.components.StatStrip
import com.taptap.ui.components.TapTapIconButton
import com.taptap.ui.components.TapTapIcons
import com.taptap.ui.components.TopBar
import com.taptap.ui.theme.CounterTheme
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
import com.taptap.util.Formatters
import com.taptap.util.currentLocale

@Composable
fun CounterRoute(
    viewModel: CounterViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onTapFeedback: () -> Unit = {},
    onSuccessFeedback: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CounterEffect.TapFeedback -> onTapFeedback()
                CounterEffect.TargetReachedFeedback -> onSuccessFeedback()
            }
        }
    }
    CounterScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onEdit = onEdit,
        modifier = modifier,
    )
}

@Composable
fun CounterScreen(
    state: CounterState,
    onIntent: (CounterIntent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // FR-7.3 / FR-3.9: the counter's own colour tints the accents; the background and its
    // scrim sit behind everything.
    CounterTheme(state.color) {
        val c = TapTapTheme.colors
        val locale = currentLocale()

        Box(modifier = modifier.fillMaxSize().background(c.bg)) {
            CounterBackground(state.background)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                TopBar(
                    title = state.name,
                    subtitle = state.notes.takeIf { it.isNotBlank() },
                    onBack = onBack,
                    backLabel = stringResource(R.string.action_back_to_home),
                    action = {
                        TapTapIconButton(
                            icon = TapTapIcons.Edit,
                            contentDescription = stringResource(R.string.a11y_edit_counter),
                            onClick = onEdit,
                            style = IconButtonStyle.Framed,
                        )
                    },
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.space4, vertical = Spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(Spacing.space4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val noValue = stringResource(R.string.counter_no_value)
                    StatStrip(
                        stats = listOf(
                            Stat(
                                Formatters.signed(state.todayChange, locale),
                                stringResource(R.string.counter_stat_today),
                            ),
                            Stat(
                                state.leftToTarget?.let { Formatters.number(it, locale) } ?: noValue,
                                stringResource(R.string.counter_stat_left),
                            ),
                            Stat(
                                state.percentDone?.let { Formatters.percent(it / 100f, locale) } ?: noValue,
                                stringResource(R.string.counter_stat_done),
                            ),
                        ),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val dialState = state.dialState()
                        CounterDial(
                            count = state.count,
                            state = dialState,
                            pillText = state.pillText(),
                            hintText = state.hintText(),
                            tapLabel = state.tapLabel(),
                            onTap = { onIntent(CounterIntent.Tap) },
                            target = state.target,
                            ofText = state.target?.let { stringResource(R.string.counter_of_target, it) },
                        )
                        Text(
                            text = state.helperText(),
                            style = TapTapType.caption,
                            color = c.inkMuted,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ControlBar(
                        isLocked = state.isLocked,
                        isReversed = state.isReversed,
                        lockLabel = stringResource(
                            if (state.isLocked) R.string.control_unlock else R.string.control_lock,
                        ),
                        reverseLabel = stringResource(
                            if (state.isReversed) R.string.control_count_up else R.string.control_reverse,
                        ),
                        resetLabel = stringResource(R.string.control_reset),
                        onToggleLock = { onIntent(CounterIntent.ToggleLock) },
                        onToggleReverse = { onIntent(CounterIntent.ToggleReverse) },
                        onResetClick = { onIntent(CounterIntent.ResetClicked) },
                    )
                }
            }

            if (state.showResetDialog) {
                ConfirmSheet(
                    title = stringResource(R.string.reset_title),
                    message = stringResource(R.string.reset_message, state.startValue),
                    confirmLabel = stringResource(R.string.reset_confirm),
                    cancelLabel = stringResource(R.string.action_cancel),
                    onConfirm = { onIntent(CounterIntent.ResetConfirmed) },
                    onDismiss = { onIntent(CounterIntent.ResetDismissed) },
                )
            }
        }
    }
}

// --- State -> copy ---------------------------------------------------------------------
// The four dial states and their exact words, from `Counter.dc.html`.

private fun CounterState.dialState(): DialState = when {
    isLocked -> DialState.Locked
    isTargetReached && !keepCountingAfterTarget && !isReversed -> DialState.TargetReached
    isReversed -> DialState.Reverse
    else -> DialState.Counting
}

@Composable
private fun CounterState.pillText(): String = when (dialState()) {
    DialState.Locked -> stringResource(R.string.counter_pill_locked)
    DialState.TargetReached -> stringResource(R.string.counter_pill_target_reached)
    DialState.Reverse -> stringResource(R.string.counter_pill_counting_down)
    DialState.Counting -> stringResource(R.string.counter_pill_step, step)
}

@Composable
private fun CounterState.hintText(): String = when (dialState()) {
    DialState.Locked -> stringResource(R.string.counter_hint_locked)
    DialState.TargetReached -> stringResource(R.string.counter_hint_done)
    DialState.Reverse -> stringResource(R.string.counter_hint_subtract, step)
    DialState.Counting -> stringResource(R.string.counter_hint_add, step)
}

@Composable
private fun CounterState.helperText(): String = when (dialState()) {
    DialState.Locked -> stringResource(R.string.counter_helper_locked)
    DialState.TargetReached -> stringResource(R.string.counter_helper_done)
    DialState.Reverse -> stringResource(R.string.counter_helper_reverse, step)
    DialState.Counting -> stringResource(R.string.counter_helper_tap)
}

@Composable
private fun CounterState.tapLabel(): String = when (dialState()) {
    DialState.Locked -> stringResource(R.string.a11y_counter_locked)
    DialState.TargetReached -> stringResource(R.string.counter_pill_target_reached)
    DialState.Reverse -> stringResource(R.string.a11y_subtract, step)
    DialState.Counting -> stringResource(R.string.a11y_add, step)
}

// --- Previews ---------------------------------------------------------------------------

private val sample = CounterState(
    isLoading = false, name = "Push-ups", notes = "Sets of 10 after the morning walk.",
    count = 32, startValue = 0, target = 50, step = 1, todayChange = 12, canTap = true,
)

@Preview(name = "Counter · counting")
@Composable
private fun CounterPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) { CounterScreen(sample, {}, {}, {}) }
}

@Preview(name = "Counter · locked")
@Composable
private fun CounterLockedPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        CounterScreen(sample.copy(isLocked = true, canTap = false), {}, {}, {})
    }
}

@Preview(name = "Counter · target reached")
@Composable
private fun CounterDonePreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        CounterScreen(sample.copy(count = 50, isTargetReached = true, canTap = false), {}, {}, {})
    }
}

@Preview(name = "Counter · teal dark with waves")
@Composable
private fun CounterDarkPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.DARK) {
        CounterScreen(
            sample.copy(color = ThemeColor.TEAL, background = Background.WAVES, isReversed = true),
            {}, {}, {},
        )
    }
}
