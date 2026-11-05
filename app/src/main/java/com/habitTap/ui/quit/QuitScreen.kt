package com.habitTap.ui.quit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitTap.R
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.BadgeState
import com.habitTap.ui.components.ButtonVariant
import com.habitTap.ui.components.ConfirmSheet
import com.habitTap.ui.components.DaysRing
import com.habitTap.ui.components.IconButtonStyle
import com.habitTap.ui.components.MilestoneBadgeData
import com.habitTap.ui.components.MilestoneRow
import com.habitTap.ui.components.PillTone
import com.habitTap.ui.components.Stat
import com.habitTap.ui.components.StatStrip
import com.habitTap.ui.components.StatusPill
import com.habitTap.ui.components.TapTapButton
import com.habitTap.ui.components.TapTapIconButton
import com.habitTap.ui.components.TapTapIcons
import com.habitTap.ui.components.TopBar
import com.habitTap.ui.theme.CounterTheme
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import com.habitTap.util.Formatters
import com.habitTap.util.TimeUtils
import com.habitTap.util.currentLocale

@Composable
fun QuitRoute(
    viewModel: QuitViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onUrgeFeedback: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                QuitEffect.UrgeFeedback -> onUrgeFeedback()
            }
        }
    }
    QuitScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onEdit = onEdit,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuitScreen(
    state: QuitState,
    onIntent: (QuitIntent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CounterTheme(state.color) {
        val c = TapTapTheme.colors
        val locale = currentLocale()
        // NFR-7: the day count is display-sized already, so its growth is capped while
        // every label around it scales normally.
        val fontScale = LocalDensity.current.fontScale
        val dayScale = if (fontScale > 1.3f) 1.3f / fontScale else 1f

        Box(modifier = modifier.fillMaxSize().background(c.bg)) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                TopBar(
                    title = state.name,
                    subtitle = if (state.daysFree > 0) {
                        stringResource(R.string.quit_free_since, Formatters.date(state.quitAt, locale))
                    } else {
                        stringResource(R.string.quit_started_again_today)
                    },
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
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.space4, vertical = Spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DaysRing(
                        progress = state.milestoneProgress,
                        modifier = Modifier.padding(top = Spacing.space2),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (state.isFocus) {
                                StatusPill(
                                    text = stringResource(R.string.quit_your_focus),
                                    tone = PillTone.Accent,
                                    icon = TapTapIcons.Focus,
                                )
                            }
                            Text(
                                text = Formatters.number(state.daysFree, locale),
                                // Capped like the counter dial: 92sp doubled fits in no ring.
                                style = TapTapType.countXl.copy(
                                    fontSize = 92.sp * dayScale,
                                    lineHeight = 96.sp * dayScale,
                                ),
                                color = c.ink,
                            )
                            Text(
                                text = stringResource(R.string.quit_days_free),
                                style = TapTapType.body.copy(fontWeight = FontWeight.SemiBold),
                                color = c.ink,
                            )
                            Text(
                                text = stringResource(
                                    R.string.quit_time_without,
                                    state.daysFree,
                                    state.hoursFree,
                                ),
                                style = TapTapType.caption,
                                color = c.inkMuted,
                            )
                        }
                    }

                    Text(
                        text = nextBadgeText(state.daysFree),
                        style = TapTapType.label,
                        color = c.accentStrong,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-6).dp),
                    )

                    StatStrip(
                        stats = listOf(
                            Stat(
                                state.moneySaved?.let { Formatters.money(it, locale) }
                                    ?: stringResource(R.string.counter_no_value),
                                stringResource(R.string.quit_stat_money),
                            ),
                            Stat(
                                Formatters.number(state.urgesTotal, locale),
                                stringResource(R.string.quit_stat_urges),
                            ),
                            Stat(
                                pluralStringResource(
                                    R.plurals.quit_best_streak_days,
                                    state.bestStreakDays,
                                    state.bestStreakDays,
                                ),
                                stringResource(R.string.quit_stat_best),
                            ),
                        ),
                        valueStyle = TapTapType.statValue,
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TapTapButton(
                            text = stringResource(R.string.quit_beat_urge),
                            onClick = { onIntent(QuitIntent.BeatUrge) },
                            variant = ButtonVariant.Primary,
                            icon = TapTapIcons.Plus,
                            minHeight = 60.dp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            // A plural, so no language ever has to render "1 times".
                            text = pluralStringResource(
                                R.plurals.quit_said_no_today,
                                state.urgesToday,
                                Formatters.number(state.urgesToday, locale),
                            ),
                            style = TapTapType.caption,
                            color = c.inkMuted,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (!state.reason.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.accentSoft, TapTapShapes.lg)
                                .padding(Spacing.space4),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.quit_your_reason),
                                style = TapTapType.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = c.onAccentSoft,
                            )
                            Text(
                                text = stringResource(R.string.quoted, state.reason),
                                style = TapTapType.statValue.copy(fontWeight = FontWeight.Medium, lineHeight = 28.sp),
                                color = c.onAccentSoft,
                            )
                        }
                    }

                    if (state.badgesOn) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Text(
                                    text = stringResource(R.string.quit_milestones),
                                    style = TapTapType.title,
                                    color = c.ink,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .semantics { heading() },
                                )
                                Text(
                                    text = stringResource(R.string.quit_badges_earned, state.earnedBadges),
                                    style = TapTapType.caption,
                                    color = c.inkMuted,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }
                            MilestoneRow(
                                badges = badgeData(state.daysFree, state.nextMilestone),
                                contentPadding = 0.dp,
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
                    ) {
                        TapTapButton(
                            text = stringResource(
                                if (state.isPinned) R.string.quit_unpin else R.string.quit_pin,
                            ),
                            onClick = { onIntent(QuitIntent.TogglePin) },
                            variant = if (state.isPinned) {
                                ButtonVariant.SecondaryActive
                            } else {
                                ButtonVariant.Secondary
                            },
                            icon = TapTapIcons.pin(state.isPinned),
                            minHeight = 52.dp,
                            modifier = Modifier.weight(1f),
                        )
                        TapTapButton(
                            text = stringResource(
                                if (state.isFocus) R.string.quit_your_focus else R.string.quit_make_focus,
                            ),
                            onClick = { onIntent(QuitIntent.ToggleFocus) },
                            variant = if (state.isFocus) {
                                ButtonVariant.SecondaryActive
                            } else {
                                ButtonVariant.Secondary
                            },
                            icon = TapTapIcons.Focus,
                            minHeight = 52.dp,
                            modifier = Modifier.weight(1f),
                        )
                        TapTapButton(
                            text = stringResource(R.string.quit_slipped),
                            onClick = { onIntent(QuitIntent.SlipClicked) },
                            variant = ButtonVariant.DangerOutline,
                            icon = TapTapIcons.Reset,
                            minHeight = 52.dp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // The bottom inset, as its own spacer. Hanging navigationBarsPadding()
                    // off a fixed height() does nothing: the outer height clamps the
                    // measurement, the inset is dropped, and the navigation bar then sits
                    // on top of the last row.
                    Spacer(Modifier.height(Spacing.space2))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }

            if (state.showSlipDialog) {
                ConfirmSheet(
                    title = stringResource(R.string.slip_title),
                    message = stringResource(R.string.slip_message),
                    confirmLabel = stringResource(R.string.slip_confirm),
                    cancelLabel = stringResource(R.string.action_cancel),
                    onConfirm = { onIntent(QuitIntent.SlipConfirmed) },
                    onDismiss = { onIntent(QuitIntent.SlipDismissed) },
                )
            }
        }
    }
}

@Composable
private fun nextBadgeText(days: Int): String {
    val next = TimeUtils.nextMilestone(days) ?: return stringResource(R.string.badge_all_earned)
    val left = next - days
    return pluralStringResource(R.plurals.badge_days_to_next, left, left, next)
}

@Composable
private fun badgeData(days: Int, next: Int?): List<MilestoneBadgeData> {
    val word = stringResource(R.string.badge_day_word)
    val earned = stringResource(R.string.badge_state_earned)
    val isNext = stringResource(R.string.badge_state_next)
    val locked = stringResource(R.string.badge_state_locked)
    return TimeUtils.MILESTONES.map { m ->
        val state = when {
            days >= m -> BadgeState.Earned
            m == next -> BadgeState.Next
            else -> BadgeState.Locked
        }
        MilestoneBadgeData(
            days = m,
            label = pluralStringResource(R.plurals.badge_days, m, m),
            state = state,
            stateLabel = when (state) {
                BadgeState.Earned -> earned
                BadgeState.Next -> isNext
                BadgeState.Locked -> locked
            },
            badgeWord = word,
        )
    }
}

// --- Previews ---------------------------------------------------------------------------

private val sample = QuitState(
    isLoading = false, name = "Quit cigarettes", quitAt = 1_760_000_000_000L,
    daysFree = 14, hoursFree = 6, nextMilestone = 21, milestoneProgress = 0.03f,
    earnedBadges = 4, moneySaved = 2100.0, urgesToday = 3, urgesTotal = 41,
    bestStreakDays = 21, reason = "To breathe better on my morning walk.", isFocus = true,
)

@Preview(name = "Quit · 14 days", heightDp = 1200)
@Composable
private fun QuitPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) { QuitScreen(sample, {}, {}, {}) }
}

@Preview(name = "Quit · day zero, no money, no reason", heightDp = 1200)
@Composable
private fun QuitZeroPreview() {
    TapTapTheme(ThemeColor.BLUE, ThemeMode.LIGHT) {
        QuitScreen(
            sample.copy(
                daysFree = 0, hoursFree = 2, nextMilestone = 1, milestoneProgress = 0.08f,
                earnedBadges = 0, moneySaved = null, reason = null, isFocus = false,
                urgesToday = 0, urgesTotal = 0, bestStreakDays = 0,
            ),
            {}, {}, {},
        )
    }
}

@Preview(name = "Quit · all badges earned (dark)", heightDp = 1200)
@Composable
private fun QuitAllBadgesPreview() {
    TapTapTheme(ThemeColor.TEAL, ThemeMode.DARK) {
        QuitScreen(
            sample.copy(daysFree = 400, nextMilestone = null, milestoneProgress = 1f, earnedBadges = 10),
            {}, {}, {},
        )
    }
}
