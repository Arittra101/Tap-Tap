package com.habitTap.ui.create

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitTap.R
import com.habitTap.model.CounterType
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.ButtonVariant
import com.habitTap.ui.components.ChipRow
import com.habitTap.ui.components.ConfirmSheet
import com.habitTap.ui.components.Segment
import com.habitTap.ui.components.SegmentedControl
import com.habitTap.ui.components.SettingDivider
import com.habitTap.ui.components.SettingGroup
import com.habitTap.ui.components.Stepper
import com.habitTap.ui.components.SwitchRow
import com.habitTap.ui.components.TapTapButton
import com.habitTap.ui.components.TapTapChip
import com.habitTap.ui.components.TapTapIcons
import com.habitTap.ui.components.TapTapTextField
import com.habitTap.ui.components.TopBar
import com.habitTap.ui.components.TypeCard
import com.habitTap.ui.journey.JourneyAnchor
import com.habitTap.ui.journey.JourneyCoach
import com.habitTap.ui.journey.JourneyStep
import com.habitTap.ui.journey.LocalJourney
import com.habitTap.ui.journey.currentJourneyStep
import com.habitTap.ui.journey.journeyAnchor
import com.habitTap.ui.journey.rememberJourneyAnchor
import com.habitTap.util.currencySymbol
import com.habitTap.util.rememberDailyMessagePermission
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import java.util.Calendar

@Composable
fun CreateRoute(
    viewModel: CreateViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    startAsQuit: Boolean = false,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(startAsQuit) {
        if (startAsQuit) viewModel.onIntent(CreateIntent.ChangeType(CounterType.QUIT))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CreateEffect.Created -> onCreated()
                CreateEffect.Leave -> onBack()
            }
        }
    }
    CreateScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Composable
fun CreateScreen(
    state: CreateState,
    onIntent: (CreateIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors

    val journey = LocalJourney.current
    val journeyStep = currentJourneyStep()
    val nameAnchor = rememberJourneyAnchor()
    val createAnchor = rememberJourneyAnchor()
    // Hoisted out of the modifier below, because the journey scrolls the form itself.
    val formScroll = rememberScrollState()

    // A step has to be able to point at something the user can see. The name field is at
    // the top of the form and the Create button is past the bottom of most screens, so
    // the journey brings each one into view as its step begins instead of ringing a
    // button that is not there yet.
    LaunchedEffect(journeyStep) {
        when (journeyStep) {
            JourneyStep.NAME_IT -> formScroll.animateScrollTo(0)
            // The button is the last thing on the form bar its footer, so the bottom of
            // the scroll is where it is.
            JourneyStep.TAP_CREATE -> formScroll.animateScrollTo(formScroll.maxValue)
            else -> Unit
        }
    }

    // The system back goes through the same intent as the arrow, so both get the sheet.
    // While the sheet is open it handles its own back press (it dismisses), so this one
    // stands down rather than re-opening what was just closed.
    BackHandler(enabled = !state.showExitSheet) { onIntent(CreateIntent.Back) }

    // Read through stringResource, not context.getString: the latter is not
    // configuration-aware, so after an in-app language change (FR-8.2) it would hand back
    // the previous language's name.
    val firstPresetName = stringResource(R.string.habit_name_cigarettes)

    // Choosing Quit fills the first preset's name, which is what the design does. Doing it
    // here rather than in the ViewModel keeps resource lookup out of the ViewModel.
    LaunchedEffect(state.type, firstPresetName) {
        if (state.type == CounterType.QUIT && state.habitPreset == null && state.name.isEmpty()) {
            onIntent(CreateIntent.PickHabitPreset(HabitPreset.CIGARETTES, firstPresetName))
        }
    }

    // A Box, so the journey's character can sit on top of the form rather than
    // becoming one more row at the bottom of it.
    Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TopBar(
                title = stringResource(R.string.create_title),
                onBack = { onIntent(CreateIntent.Back) },
                backLabel = stringResource(R.string.action_back),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    // imePadding has to come BEFORE verticalScroll: it must shrink the scroll
                    // viewport, not pad the content inside it. The other way round, the
                    // viewport still runs behind the keyboard and the focused field is hidden.
                    .imePadding()
                    .verticalScroll(formScroll)
                    .padding(horizontal = Spacing.space4, vertical = Spacing.space6),
                verticalArrangement = Arrangement.spacedBy(Spacing.space6),
            ) {
                TypeSection(state, onIntent)

                if (state.isQuit) {
                    QuitFields(state, onIntent)
                } else {
                    NormalFields(state, onIntent, nameAnchor)
                }

                TapTapButton(
                    text = stringResource(
                        if (state.isQuit) R.string.create_button_quit else R.string.create_button_normal,
                    ),
                    onClick = { onIntent(CreateIntent.Create) },
                    variant = ButtonVariant.Primary,
                    enabled = state.canCreate,
                    minHeight = 56.dp,
                    modifier = Modifier.fillMaxWidth().journeyAnchor(createAnchor),
                )

                Text(
                    text = stringResource(R.string.create_footer),
                    style = TapTapType.caption,
                    color = c.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                // The bottom inset, as its own spacer. Hanging navigationBarsPadding()
                // off a fixed height() does nothing: the outer height clamps the
                // measurement, the inset is dropped, and the navigation bar then sits
                // on top of the last row.
                Spacer(Modifier.height(Spacing.space4))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // The journey's two form steps. Only on the normal half: the journey teaches a
        // normal counter, and the quit form is a different shape with no step for it.
        if (!state.isQuit) {
            when (journeyStep) {
                JourneyStep.NAME_IT -> JourneyCoach(
                    text = stringResource(R.string.journey_name),
                    anchor = nameAnchor,
                    onStop = { journey?.stop() },
                    // Next appears once there is a title to go on with. Only the user
                    // knows when they have finished typing, so the step waits for them
                    // to say so rather than jumping on the first letter.
                    onNext = if (state.name.isNotBlank()) {
                        { journey?.onNamed() }
                    } else {
                        null
                    },
                )
                JourneyStep.TAP_CREATE -> JourneyCoach(
                    text = stringResource(R.string.journey_create_button),
                    anchor = createAnchor,
                    onStop = { journey?.stop() },
                )
                else -> Unit
            }
        }

        // Leaving with a title typed and nothing saved: the sheet offers the create the
        // form's own button would have done, so back is never a silent loss.
        if (state.showExitSheet) {
            ConfirmSheet(
                title = stringResource(R.string.create_exit_title),
                message = stringResource(R.string.create_exit_message),
                confirmLabel = stringResource(R.string.create_button_normal),
                cancelLabel = stringResource(R.string.action_discard),
                onConfirm = { onIntent(CreateIntent.Create) },
                onDismiss = { onIntent(CreateIntent.ExitSheetDismissed) },
                confirmVariant = ButtonVariant.Primary,
                // Discard leaves. The scrim and a swipe down only close the sheet, so a
                // stray tap outside it never costs the user the form.
                onCancel = { onIntent(CreateIntent.Discard) },
            )
        }
    }
}

@Composable
private fun TypeSection(state: CreateState, onIntent: (CreateIntent) -> Unit) {
    val c = TapTapTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        Text(
            text = stringResource(R.string.create_type_question),
            style = TapTapType.title,
            color = c.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            TypeCard(
                title = stringResource(R.string.create_type_normal),
                subtitle = stringResource(R.string.create_type_normal_sub),
                icon = TapTapIcons.Plus,
                selected = !state.isQuit,
                onClick = { onIntent(CreateIntent.ChangeType(CounterType.NORMAL)) },
                modifier = Modifier.weight(1f),
            )
            TypeCard(
                title = stringResource(R.string.create_type_quit),
                subtitle = stringResource(R.string.create_type_quit_sub),
                icon = TapTapIcons.Quit,
                selected = state.isQuit,
                onClick = { onIntent(CreateIntent.ChangeType(CounterType.QUIT)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NormalFields(
    state: CreateState,
    onIntent: (CreateIntent) -> Unit,
    nameAnchor: JourneyAnchor? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space6)) {
        TapTapTextField(
            value = state.name,
            onValueChange = { onIntent(CreateIntent.ChangeName(it)) },
            label = stringResource(R.string.create_name_normal),
            placeholder = stringResource(R.string.create_name_hint_normal),
            required = true,
            modifier = if (nameAnchor != null) Modifier.journeyAnchor(nameAnchor) else Modifier,
        )

        SettingGroup {
            Stepper(
                label = stringResource(R.string.field_start_value),
                help = stringResource(R.string.field_start_value_help),
                value = state.startValue,
                onValueChange = { onIntent(CreateIntent.ChangeStartValue(it)) },
                decreaseLabel = stringResource(R.string.a11y_lower_start_value),
                increaseLabel = stringResource(R.string.a11y_raise_start_value),
                min = 0,
                modifier = Modifier.padding(Spacing.space4),
            )
            SettingDivider()
            Stepper(
                label = stringResource(R.string.field_step_value),
                help = stringResource(R.string.field_step_value_help),
                value = state.step.toLong(),
                onValueChange = { onIntent(CreateIntent.ChangeStep(it.toInt())) },
                decreaseLabel = stringResource(R.string.a11y_lower_step_value),
                increaseLabel = stringResource(R.string.a11y_raise_step_value),
                min = 1,
                modifier = Modifier.padding(Spacing.space4),
            )
        }

        TargetGroup(state, onIntent, isQuit = false)

        TapTapTextField(
            value = state.notes,
            onValueChange = { onIntent(CreateIntent.ChangeNotes(it)) },
            label = stringResource(R.string.field_notes),
            optionalLabel = stringResource(R.string.label_optional),
            placeholder = stringResource(R.string.field_notes_hint),
            singleLine = false,
            minHeight = 88.dp,
        )

        SettingGroup {
            SwitchRow(
                label = stringResource(R.string.field_pin_to_top),
                help = stringResource(R.string.field_pin_help_normal),
                checked = state.isPinned,
                onCheckedChange = { onIntent(CreateIntent.TogglePin(it)) },
            )
        }
    }
}

@Composable
private fun QuitFields(state: CreateState, onIntent: (CreateIntent) -> Unit) {
    val c = TapTapTheme.colors
    val context = LocalContext.current
    val notificationPermission = rememberDailyMessagePermission()

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space6)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            Text(
                text = stringResource(R.string.create_pick_habit),
                style = TapTapType.label,
                color = c.ink,
            )
            ChipRow {
                HabitPreset.all.forEach { id ->
                    val label = stringResource(HabitPreset.labelResFor(id))
                    val nameRes = HabitPreset.nameResFor(id)
                    val name = if (nameRes != null) stringResource(nameRes) else ""
                    TapTapChip(
                        label = label,
                        selected = state.habitPreset == id,
                        onClick = { onIntent(CreateIntent.PickHabitPreset(id, name)) },
                    )
                }
            }
        }

        TapTapTextField(
            value = state.name,
            onValueChange = { onIntent(CreateIntent.ChangeName(it)) },
            label = stringResource(R.string.create_name_quit),
            placeholder = stringResource(R.string.create_name_hint_quit),
            required = true,
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            Text(
                text = stringResource(R.string.create_when_stopped),
                style = TapTapType.label,
                color = c.ink,
            )
            SegmentedControl(
                segments = listOf(
                    Segment(true, stringResource(R.string.create_today)),
                    Segment(false, stringResource(R.string.create_earlier_date)),
                ),
                selected = state.quitToday,
                onSelect = { today ->
                    if (today) {
                        onIntent(CreateIntent.ChangeQuitToday(true))
                    } else {
                        showDatePicker(context, state.quitDateMillis) {
                            onIntent(CreateIntent.ChangeQuitDate(it))
                        }
                    }
                },
            )
            Text(
                text = stringResource(R.string.create_quit_date_help),
                style = TapTapType.caption,
                color = c.inkMuted,
            )
        }

        TapTapTextField(
            value = state.moneyPerDay,
            onValueChange = { onIntent(CreateIntent.ChangeMoneyPerDay(it)) },
            label = stringResource(R.string.create_money_per_day),
            optionalLabel = stringResource(R.string.label_optional),
            placeholder = stringResource(R.string.create_money_hint),
            help = stringResource(R.string.create_money_help),
            keyboardType = KeyboardType.Decimal,
            prefix = currencySymbol(),
        )

        TargetGroup(state, onIntent, isQuit = true)

        TapTapTextField(
            value = state.reason,
            onValueChange = { onIntent(CreateIntent.ChangeReason(it)) },
            label = stringResource(R.string.create_reason),
            optionalLabel = stringResource(R.string.label_optional),
            placeholder = stringResource(R.string.create_reason_hint),
            help = stringResource(R.string.create_reason_help),
            singleLine = false,
            minHeight = 88.dp,
        )

        SettingGroup(heading = stringResource(R.string.create_motivation)) {
            SwitchRow(
                label = stringResource(R.string.field_badges),
                help = stringResource(R.string.field_badges_help),
                checked = state.badgesOn,
                onCheckedChange = { onIntent(CreateIntent.ToggleBadges(it)) },
            )
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_daily_message),
                help = stringResource(R.string.field_daily_message_help),
                checked = state.dailyMessageOn,
                // FR-8.6: ask for notifications here, and leave the switch off if refused.
                onCheckedChange = { wantOn ->
                    notificationPermission.request(wantOn) { allowed ->
                        onIntent(CreateIntent.ToggleDailyMessage(allowed))
                    }
                },
            )
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_make_focus),
                help = stringResource(R.string.field_make_focus_help),
                checked = state.makeFocus,
                onCheckedChange = { onIntent(CreateIntent.ToggleMakeFocus(it)) },
            )
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_pin_to_top),
                help = stringResource(R.string.field_pin_help_quit),
                checked = state.isPinned,
                onCheckedChange = { onIntent(CreateIntent.TogglePin(it)) },
            )
        }
    }
}

/** The target switch plus its stepper; the labels differ between the two types. */
@Composable
private fun TargetGroup(state: CreateState, onIntent: (CreateIntent) -> Unit, isQuit: Boolean) {
    val on = if (isQuit) state.goalOn else state.targetOn
    SettingGroup {
        SwitchRow(
            label = stringResource(if (isQuit) R.string.field_set_goal else R.string.field_set_target),
            checked = on,
            onCheckedChange = {
                onIntent(if (isQuit) CreateIntent.ToggleGoal(it) else CreateIntent.ToggleTarget(it))
            },
        )
        if (on) {
            SettingDivider()
            Stepper(
                label = stringResource(
                    if (isQuit) R.string.field_days_to_stay_free else R.string.field_target_count,
                ),
                value = if (isQuit) state.goalDays.toLong() else state.target,
                onValueChange = {
                    onIntent(
                        if (isQuit) CreateIntent.ChangeGoalDays(it.toInt())
                        else CreateIntent.ChangeTarget(it),
                    )
                },
                decreaseLabel = stringResource(R.string.a11y_less),
                increaseLabel = stringResource(R.string.a11y_more),
                min = 1,
                modifier = Modifier.padding(Spacing.space4),
            )
        }
        if (!isQuit && state.targetOn) {
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_keep_counting),
                help = stringResource(R.string.field_keep_counting_help),
                checked = state.keepCountingAfterTarget,
                onCheckedChange = { onIntent(CreateIntent.ToggleKeepCounting(it)) },
            )
        }
    }
}

/** FR-2.3: today or an earlier date, never the future. */
private fun showDatePicker(
    context: android.content.Context,
    currentMillis: Long?,
    onPicked: (Long) -> Unit,
) {
    val calendar = Calendar.getInstance().apply {
        if (currentMillis != null) timeInMillis = currentMillis
    }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val picked = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onPicked(picked.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }.show()
}

@Preview(name = "New counter · Normal", heightDp = 1500)
@Composable
private fun CreateNormalPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        CreateScreen(CreateState(type = CounterType.NORMAL, name = "Push-ups", targetOn = true), {})
    }
}

@Preview(name = "New counter · Quit (dark)", heightDp = 1700)
@Composable
private fun CreateQuitPreview() {
    TapTapTheme(ThemeColor.TEAL, ThemeMode.DARK) {
        CreateScreen(
            CreateState(
                type = CounterType.QUIT,
                habitPreset = HabitPreset.CIGARETTES,
                name = "Quit cigarettes",
                moneyPerDay = "150",
            ),
            {},
        )
    }
}
