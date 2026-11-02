package com.taptap.ui.create

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.taptap.R
import com.taptap.model.CounterType
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import com.taptap.ui.components.ButtonVariant
import com.taptap.ui.components.ChipRow
import com.taptap.ui.components.Segment
import com.taptap.ui.components.SegmentedControl
import com.taptap.ui.components.SettingDivider
import com.taptap.ui.components.SettingGroup
import com.taptap.ui.components.Stepper
import com.taptap.ui.components.SwitchRow
import com.taptap.ui.components.TapTapButton
import com.taptap.ui.components.TapTapChip
import com.taptap.ui.components.TapTapIcons
import com.taptap.ui.components.TapTapTextField
import com.taptap.ui.components.TopBar
import com.taptap.ui.components.TypeCard
import com.taptap.util.currencySymbol
import com.taptap.util.rememberDailyMessagePermission
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
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
            }
        }
    }
    CreateScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack, modifier = modifier)
}

@Composable
fun CreateScreen(
    state: CreateState,
    onIntent: (CreateIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding(),
    ) {
        TopBar(
            title = stringResource(R.string.create_title),
            onBack = onBack,
            backLabel = stringResource(R.string.action_back),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                // imePadding has to come BEFORE verticalScroll: it must shrink the scroll
                // viewport, not pad the content inside it. The other way round, the
                // viewport still runs behind the keyboard and the focused field is hidden.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space4, vertical = Spacing.space6),
            verticalArrangement = Arrangement.spacedBy(Spacing.space6),
        ) {
            TypeSection(state, onIntent)

            if (state.isQuit) {
                QuitFields(state, onIntent)
            } else {
                NormalFields(state, onIntent)
            }

            TapTapButton(
                text = stringResource(
                    if (state.isQuit) R.string.create_button_quit else R.string.create_button_normal,
                ),
                onClick = { onIntent(CreateIntent.Create) },
                variant = ButtonVariant.Primary,
                enabled = state.canCreate,
                minHeight = 56.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.create_footer),
                style = TapTapType.caption,
                color = c.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.space4).navigationBarsPadding())
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
private fun NormalFields(state: CreateState, onIntent: (CreateIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space6)) {
        TapTapTextField(
            value = state.name,
            onValueChange = { onIntent(CreateIntent.ChangeName(it)) },
            label = stringResource(R.string.create_name_normal),
            placeholder = stringResource(R.string.create_name_hint_normal),
            required = true,
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
        CreateScreen(CreateState(type = CounterType.NORMAL, name = "Push-ups", targetOn = true), {}, {})
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
            {},
        )
    }
}
