package com.taptap.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taptap.R
import com.taptap.model.Counter
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import com.taptap.ui.components.ButtonVariant
import com.taptap.ui.components.CounterCard
import com.taptap.ui.components.FocusCard
import com.taptap.ui.components.FocusCardLabels
import com.taptap.ui.components.FocusPicker
import com.taptap.ui.components.HomeTopBar
import com.taptap.ui.components.NewCounterCard
import com.taptap.ui.components.QuitCard
import com.taptap.ui.components.QuoteSlider
import com.taptap.ui.components.TapTapButton
import com.taptap.ui.components.TapTapIcons
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
import com.taptap.util.Formatters
import com.taptap.util.TimeUtils
import com.taptap.util.currentLocale

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onOpenCounter: (Long) -> Unit,
    onOpenQuit: (Long) -> Unit,
    onCreate: (quit: Boolean) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onTapFeedback: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.TapFeedback -> onTapFeedback()
            }
        }
    }
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenCounter = onOpenCounter,
        onOpenQuit = onOpenQuit,
        onCreate = onCreate,
        onSettings = onSettings,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onOpenCounter: (Long) -> Unit,
    onOpenQuit: (Long) -> Unit,
    onCreate: (quit: Boolean) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors

    Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            HomeTopBar(
                onSettings = onSettings,
                settingsLabel = stringResource(R.string.a11y_settings),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = Spacing.space6),
                // Between sections. A section's own header-to-content gap is
                // tighter (see HomeSection), so each title reads as belonging to the
                // list under it rather than floating between two.
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Always on, first run included. Tapping it opens New counter with the
                // quit type already chosen.
                QuoteSlider(
                    quotes = stringArrayResource(R.array.quit_motivation_quotes).toList(),
                    ctaText = stringResource(R.string.quote_cta),
                    onClick = { onCreate(true) },
                    modifier = Modifier.padding(horizontal = Spacing.space4),
                )

                when {
                    // FR-1.10: first open with no counters.
                    state.isEmpty -> EmptyState(onCreate)

                    else -> {
                        // FR-1.2 / FR-1.3 / FR-1.4: focus card, picker, or nothing at all.
                        state.focus?.let { focus ->
                            FocusSection(focus, state, onIntent, onOpenQuit)
                        }
                        if (state.showFocusPicker) {
                            FocusPickerSection(state.allQuitCounters, onIntent)
                        }
                        CountersSection(state, onIntent, onOpenCounter, onCreate)
                        QuitSection(state, onIntent, onOpenQuit)
                    }
                }
                // Clears the floating button.
                Spacer(Modifier.height(if (state.isEmpty) Spacing.space6 else 120.dp))
                // ...and then the navigation bar. Only the button carried this inset
                // before, so the end of a long list slid underneath the bar.
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // FR-1.9 / FR-1.10: hidden on first run, when the two big choices are the action.
        if (!state.isEmpty) {
            TapTapButton(
                text = stringResource(R.string.home_new_counter),
                onClick = { onCreate(false) },
                variant = ButtonVariant.Fab,
                icon = TapTapIcons.Plus,
                minHeight = 56.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = Spacing.space4, bottom = 28.dp),
            )
        }
    }
}

// --- Focus -----------------------------------------------------------------------------

@Composable
private fun FocusSection(
    focus: Counter,
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onOpenQuit: (Long) -> Unit,
) {
    val locale = currentLocale()
    val days = TimeUtils.daysFree(focus.quitAt ?: state.nowMillis, state.nowMillis)
    val hours = TimeUtils.hoursFree(focus.quitAt ?: state.nowMillis, state.nowMillis)
    val money = TimeUtils.moneySaved(days, focus.moneyPerDay)

    HomeSection(
        title = stringResource(R.string.home_focus_heading),
        trailing = stringResource(R.string.home_focus_one_at_a_time),
    ) {
    Box(Modifier.padding(horizontal = Spacing.space4)) {
        FocusCard(
            counter = focus,
            daysFree = days,
            milestoneProgress = TimeUtils.milestoneProgress(days, hours),
            nextBadgeText = nextBadgeText(days),
            moneySavedText = money?.let { Formatters.money(it, locale) } ?: "—",
            urgesTodayText = Formatters.number(state.focusUrgesToday, locale),
            sectionLabels = FocusCardLabels(
                daysFree = stringResource(R.string.home_days_free),
                moneySaved = stringResource(R.string.home_money_saved),
                urgesToday = stringResource(R.string.home_urges_today),
                beatUrge = stringResource(R.string.home_beat_urge),
                open = stringResource(R.string.a11y_open_counter, focus.name),
            ),
            onOpen = { onOpenQuit(focus.id) },
            onBeatUrge = { onIntent(HomeIntent.BeatUrge(focus.id)) },
        )
    }
    }
}

@Composable
private fun FocusPickerSection(quitCounters: List<Counter>, onIntent: (HomeIntent) -> Unit) {
    HomeSection(
        title = stringResource(R.string.home_focus_heading),
        trailing = stringResource(R.string.home_focus_not_set),
    ) {
    Box(Modifier.padding(horizontal = Spacing.space4)) {
        FocusPicker(
            quitCounters = quitCounters,
            title = stringResource(R.string.home_focus_pick_title),
            help = stringResource(R.string.home_focus_pick_help),
            onPick = { onIntent(HomeIntent.MakeFocus(it)) },
        )
    }
    }
}

// --- Counters --------------------------------------------------------------------------

@Composable
private fun CountersSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onOpenCounter: (Long) -> Unit,
    onCreate: (quit: Boolean) -> Unit,
) {
    HomeSection(
        title = stringResource(R.string.home_counters_heading),
        trailing = "${
            pluralStringResource(
                R.plurals.home_counter_count,
                state.normalCounters.size,
                state.normalCounters.size,
            )
        } ${stringResource(R.string.home_pinned_first)}",
    ) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.space4, vertical = Spacing.space1),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
        verticalAlignment = Alignment.Top,
    ) {
        state.normalCounters.forEach { counter ->
            val done = counter.isTargetReached && !counter.keepCountingAfterTarget
            CounterCard(
                counter = counter,
                onOpen = { onOpenCounter(counter.id) },
                onAdd = { onIntent(HomeIntent.AddTap(counter.id)) },
                onTogglePin = { onIntent(HomeIntent.TogglePin(counter.id)) },
                addLabel = stringResource(R.string.a11y_add_to, counter.step, counter.name),
                pinLabel = stringResource(
                    if (counter.isPinned) R.string.a11y_unpin else R.string.a11y_pin,
                    counter.name,
                ),
                targetReachedLabel = stringResource(R.string.home_target_reached),
                openLabel = stringResource(R.string.a11y_open_counter, counter.name),
                metaText = if (done) {
                    stringResource(R.string.home_target_reached)
                } else {
                    stringResource(
                        if (counter.isReversed) {
                            R.string.home_step_meta_down
                        } else {
                            R.string.home_step_meta
                        },
                        counter.step,
                    )
                },
                ofText = counter.target
                    ?.let { stringResource(R.string.home_of_target, it) }
                    ?: stringResource(R.string.home_no_target),
            )
        }
        NewCounterCard(
            label = stringResource(R.string.home_new_counter),
            onClick = { onCreate(false) },
        )
    }
    }
}

// --- Quit tracker ----------------------------------------------------------------------

@Composable
private fun QuitSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onOpenQuit: (Long) -> Unit,
) {
    if (state.quitCounters.isEmpty()) return
    val locale = currentLocale()

    HomeSection(
        title = stringResource(R.string.home_quit_heading),
        trailing = "${
            pluralStringResource(
                R.plurals.home_habit_count,
                state.quitCounters.size,
                state.quitCounters.size,
            )
        } ${stringResource(R.string.home_pinned_first)}",
    ) {
    Column(
        modifier = Modifier.padding(horizontal = Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        state.quitCounters.forEach { counter ->
            val days = TimeUtils.daysFree(counter.quitAt ?: state.nowMillis, state.nowMillis)
            val hours = TimeUtils.hoursFree(counter.quitAt ?: state.nowMillis, state.nowMillis)
            val money = TimeUtils.moneySaved(days, counter.moneyPerDay)
            val urges = state.quitUrgesToday[counter.id] ?: 0

            QuitCard(
                counter = counter,
                daysFree = days,
                daysFreeLabel = stringResource(R.string.home_days_free),
                // The design swaps money for urges when no daily spend is set, rather
                // than showing a meaningless zero.
                sideText = if (money != null) {
                    stringResource(R.string.home_money_saved_value, Formatters.money(money, locale))
                } else {
                    pluralStringResource(R.plurals.home_urges_beaten_today, urges, urges)
                },
                nextBadgeText = nextBadgeText(days),
                milestoneProgress = TimeUtils.milestoneProgress(days, hours),
                onOpen = { onOpenQuit(counter.id) },
                onMakeFocus = { onIntent(HomeIntent.MakeFocus(counter.id)) },
                onTogglePin = { onIntent(HomeIntent.TogglePin(counter.id)) },
                makeFocusLabel = stringResource(R.string.a11y_make_focus, counter.name),
                makeFocusText = stringResource(R.string.home_make_focus_short),
                openLabel = stringResource(R.string.a11y_open_counter, counter.name),
                pinLabel = stringResource(
                    if (counter.isPinned) R.string.a11y_unpin else R.string.a11y_pin,
                    counter.name,
                ),
            )
        }
    }
    }
}

// --- First run -------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyState(onCreate: (quit: Boolean) -> Unit) {
    val c = TapTapTheme.colors
    Column(
        modifier = Modifier.padding(horizontal = Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        com.taptap.ui.components.WelcomeMark()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            Text(
                text = stringResource(R.string.home_welcome_title),
                style = TapTapType.screenTitle.copy(fontSize = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp)),
                color = c.ink,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.home_welcome_body),
                style = TapTapType.body,
                color = c.inkMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(300.dp),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3),
        ) {
            com.taptap.ui.components.BigChoiceRow(
                title = stringResource(R.string.home_choice_normal_title),
                subtitle = stringResource(R.string.home_choice_normal_sub),
                icon = TapTapIcons.Plus,
                accentFill = true,
                onClick = { onCreate(false) },
            )
            com.taptap.ui.components.BigChoiceRow(
                title = stringResource(R.string.home_choice_quit_title),
                subtitle = stringResource(R.string.home_choice_quit_sub),
                icon = TapTapIcons.Quit,
                accentFill = false,
                onClick = { onCreate(true) },
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.home_popular_ideas),
                style = TapTapType.label,
                color = c.inkMuted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                verticalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                listOf(
                    R.string.idea_pushups,
                    R.string.idea_water,
                    R.string.idea_pages,
                    R.string.idea_quit_smoking,
                    R.string.idea_phone_night,
                ).forEach { res ->
                    com.taptap.ui.components.TapTapChip(
                        label = stringResource(res),
                        selected = false,
                        // The two quit ideas open the quit form, like the big choice above.
                        onClick = { onCreate(res == R.string.idea_quit_smoking || res == R.string.idea_phone_night) },
                    )
                }
            }
        }
    }
}

// --- Shared ----------------------------------------------------------------------------

/** A section: its header tight against its own content. */
@Composable
private fun HomeSection(
    title: String,
    trailing: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title, trailing = trailing)
        content()
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    val c = TapTapTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.space4),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = TapTapType.title,
            color = c.ink,
            // Both children take a share of the row. With SpaceBetween alone they overrun
            // each other at 200% font scale (NFR-7).
            modifier = Modifier
                .weight(1f, fill = false)
                .semantics { heading() },
        )
        Text(
            text = trailing,
            style = TapTapType.caption,
            color = c.inkMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun nextBadgeText(days: Int): String {
    val next = TimeUtils.nextMilestone(days)
        ?: return stringResource(R.string.badge_all_earned)
    val left = next - days
    return pluralStringResource(R.plurals.badge_days_to_next, left, left, next)
}

// --- Previews --------------------------------------------------------------------------

@Preview(name = "Home · focus", heightDp = 1240)
@Composable
private fun HomeFocusPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        HomeScreen(HomePreviewData.withFocus, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Home · no focus", heightDp = 1250)
@Composable
private fun HomeNoFocusPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        HomeScreen(HomePreviewData.noFocus, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Home · first run", heightDp = 900)
@Composable
private fun HomeEmptyPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        HomeScreen(HomeState(isLoading = false, isEmpty = true), {}, {}, {}, {}, {})
    }
}

@Preview(name = "Home · saffron dark", heightDp = 1240)
@Composable
private fun HomeDarkPreview() {
    TapTapTheme(ThemeColor.SAFFRON, ThemeMode.DARK) {
        HomeScreen(HomePreviewData.withFocus, {}, {}, {}, {}, {})
    }
}
