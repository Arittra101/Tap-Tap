package com.habitTap.ui.home

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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.habitTap.R
import com.habitTap.model.Counter
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.ButtonVariant
import com.habitTap.ui.components.CounterCard
import com.habitTap.ui.components.FocusCard
import com.habitTap.ui.components.FocusCardLabels
import com.habitTap.ui.components.FocusPicker
import com.habitTap.ui.components.HomeTopBar
import com.habitTap.ui.components.NewCounterCard
import com.habitTap.ui.components.QuitCard
import com.habitTap.ui.components.QuoteSlider
import com.habitTap.ui.components.TapTapButton
import com.habitTap.ui.components.TapTapIcons
import com.habitTap.ui.journey.JourneyAnchor
import com.habitTap.ui.journey.JourneyCoach
import com.habitTap.ui.journey.JourneyStep
import com.habitTap.ui.journey.LocalJourney
import com.habitTap.ui.journey.currentJourneyStep
import com.habitTap.ui.journey.journeyAnchor
import com.habitTap.ui.journey.rememberJourneyAnchor
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import com.habitTap.util.Formatters
import com.habitTap.util.TimeUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import com.habitTap.util.currencyCode
import com.habitTap.util.currentLocale

/**
 * Where Home's page sits and where the quit list starts inside it.
 *
 * Both in root coordinates, because that is what a layout can report about itself. The
 * difference between them, plus the current scroll, is the offset the quit list would
 * need to be at the top of the page — which is a thing no one can know until Home has
 * been laid out, and the scrolls all arrive before that.
 */
class HomeScrollAnchors {
    var viewportTopInRoot: Float by mutableFloatStateOf(Float.NaN)
    var quitSectionTopInRoot: Float by mutableFloatStateOf(Float.NaN)

    /** How far down the page the quit list starts, or null before either is measured. */
    val quitListTop: Int?
        get() = if (viewportTopInRoot.isNaN() || quitSectionTopInRoot.isNaN()) {
            null
        } else {
            (quitSectionTopInRoot - viewportTopInRoot).toInt()
        }
}

/** How long a scroll waits for Home to be measured before giving up on it. */
private const val ScrollWaitMillis = 2_000L

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
    // Hoisted, because an effect scrolls it.
    val scrollState = rememberScrollState()
    val anchors = remember { HomeScrollAnchors() }
    // The quit list stops just below the top edge rather than flush against it, so the
    // card above it is still half visible and the page reads as scrolled, not jumped.
    val quitTopGap = with(LocalDensity.current) { Spacing.space3.roundToPx() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.TapFeedback -> onTapFeedback()
                // Each of these arrives while Home is off screen, so the new content has
                // not been measured yet: the end of the page is still 0 and the quit list
                // has no position. Each waits for what it needs, and none waits forever —
                // a page that fits needs no scrolling, and a collector stuck here would
                // stop every later effect, tap feedback included.
                HomeEffect.ScrollToEnd -> {
                    withTimeoutOrNull(ScrollWaitMillis) {
                        snapshotFlow { scrollState.maxValue }.first { it > 0 }
                    }
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
                HomeEffect.ScrollToQuitTop -> {
                    withTimeoutOrNull(ScrollWaitMillis) {
                        snapshotFlow { anchors.quitListTop }.first { it != null }
                    }
                    anchors.quitListTop?.let { top ->
                        scrollState.animateScrollTo(
                            (scrollState.value + top - quitTopGap)
                                .coerceIn(0, scrollState.maxValue),
                        )
                    }
                }
                HomeEffect.ScrollToTop -> scrollState.animateScrollTo(0)
            }
        }
    }
    HomeScreen(
        state = state,
        scrollState = scrollState,
        anchors = anchors,
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
    scrollState: ScrollState = rememberScrollState(),
    anchors: HomeScrollAnchors = remember { HomeScrollAnchors() },
) {
    val c = TapTapTheme.colors

    // The guided journey, when one is running. Null in previews and in tests, which is
    // why none of this needs a parameter on the screen.
    val journey = LocalJourney.current
    val journeyStep = currentJourneyStep()
    // Whichever of the two leads to the normal form on this Home: the big choice on first
    // run, the floating button once counters exist.
    val createAnchor = rememberJourneyAnchor()
    val firstCardAnchor = rememberJourneyAnchor()

    // The journey's last step is answered by a tap on a card, so it watches the intents
    // on their way through rather than asking the screen to report separately.
    val handleIntent: (HomeIntent) -> Unit = { intent ->
        if (intent is HomeIntent.AddTap) journey?.onCounterTapped()
        onIntent(intent)
    }

    Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            HomeTopBar(
                onSettings = onSettings,
                settingsLabel = stringResource(R.string.a11y_settings),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { anchors.viewportTopInRoot = it.positionInRoot().y }
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
                    state.isEmpty -> EmptyState(onCreate, createAnchor)

                    else -> {
                        // FR-1.2 / FR-1.3 / FR-1.4: focus card, picker, or nothing at all.
                        state.focus?.let { focus ->
                            FocusSection(focus, state, handleIntent, onOpenQuit)
                        }
                        if (state.showFocusPicker) {
                            FocusPickerSection(state.allQuitCounters, handleIntent)
                        }
                        CountersSection(
                            state,
                            handleIntent,
                            onOpenCounter,
                            onCreate,
                            firstCardAnchor,
                            // The last step rings the whole card, so the card body is
                            // inside the one area still taking touches. Opening the
                            // counter from there would walk the user off the step they
                            // are being shown, so while it is up the card only counts.
                            holdFirstCard = journeyStep == JourneyStep.FIRST_TAP,
                        )
                        QuitSection(
                            state,
                            handleIntent,
                            onOpenQuit,
                            Modifier.onGloballyPositioned {
                                anchors.quitSectionTopInRoot = it.positionInRoot().y
                            },
                        )
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
                    .padding(end = Spacing.space4, bottom = 28.dp)
                    .journeyAnchor(createAnchor),
            )
        }

        // On top of Home, and only ever one step at a time.
        when (journeyStep) {
            JourneyStep.CREATE_COUNTER -> JourneyCoach(
                text = stringResource(R.string.journey_create),
                anchor = createAnchor,
                onStop = { journey?.stop() },
            )
            JourneyStep.FIRST_TAP -> JourneyCoach(
                text = stringResource(R.string.journey_first_tap),
                anchor = firstCardAnchor,
                onStop = { journey?.stop() },
            )
            // The middle two steps belong to the New counter form, not here.
            else -> Unit
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
    val currency = currencyCode()
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
            moneySavedText = money?.let { Formatters.money(it, locale, currency) } ?: "—",
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
    firstCardAnchor: JourneyAnchor,
    /** The journey is pointing at the first card: its + counts, the rest of it does not. */
    holdFirstCard: Boolean = false,
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
        state.normalCounters.forEachIndexed { index, counter ->
            val done = counter.isTargetReached && !counter.keepCountingAfterTarget
            val held = holdFirstCard && index == 0
            CounterCard(
                counter = counter,
                onOpen = { if (!held) onOpenCounter(counter.id) },
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
                // The journey's last step points at the first card, which is the one a
                // user with a single counter has just made.
                modifier = if (index == 0) Modifier.journeyAnchor(firstCardAnchor) else Modifier,
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
    modifier: Modifier = Modifier,
) {
    if (state.quitCounters.isEmpty()) return
    val locale = currentLocale()
    val currency = currencyCode()

    HomeSection(
        title = stringResource(R.string.home_quit_heading),
        trailing = "${
            pluralStringResource(
                R.plurals.home_habit_count,
                state.quitCounters.size,
                state.quitCounters.size,
            )
        } ${stringResource(R.string.home_pinned_first)}",
        // Reports where the quit list starts, for the scroll that shows its first card.
        modifier = modifier,
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
                    stringResource(
                        R.string.home_money_saved_value,
                        Formatters.money(money, locale, currency),
                    )
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
private fun EmptyState(onCreate: (quit: Boolean) -> Unit, normalChoiceAnchor: JourneyAnchor) {
    val c = TapTapTheme.colors
    Column(
        modifier = Modifier.padding(horizontal = Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        com.habitTap.ui.components.WelcomeMark()

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
            com.habitTap.ui.components.BigChoiceRow(
                title = stringResource(R.string.home_choice_normal_title),
                subtitle = stringResource(R.string.home_choice_normal_sub),
                icon = TapTapIcons.Plus,
                accentFill = true,
                onClick = { onCreate(false) },
                modifier = Modifier.journeyAnchor(normalChoiceAnchor),
            )
            com.habitTap.ui.components.BigChoiceRow(
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
                    com.habitTap.ui.components.TapTapChip(
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
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
