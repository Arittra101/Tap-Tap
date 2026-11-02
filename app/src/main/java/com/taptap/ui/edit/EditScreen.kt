package com.taptap.ui.edit

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taptap.R
import com.taptap.model.Background
import com.taptap.model.CounterType
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import com.taptap.ui.components.BackgroundTile
import com.taptap.ui.components.ButtonVariant
import com.taptap.ui.components.ColorSwatch
import com.taptap.ui.components.ConfirmSheet
import com.taptap.ui.components.Segment
import com.taptap.ui.components.SegmentedControl
import com.taptap.ui.components.SettingDivider
import com.taptap.ui.components.SettingGroup
import com.taptap.ui.components.Stepper
import com.taptap.ui.components.SwitchRow
import com.taptap.ui.components.TapTapButton
import com.taptap.ui.components.TapTapTextField
import com.taptap.ui.components.TopBar
import com.taptap.ui.components.ValueRow
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
import com.taptap.ui.theme.swatch
import com.taptap.util.Formatters
import com.taptap.util.currencySymbol
import com.taptap.util.rememberDailyMessagePermission
import com.taptap.util.currentLocale
import java.util.Calendar

@Composable
fun EditRoute(
    viewModel: EditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditEffect.Saved -> onSaved()
                EditEffect.Deleted -> onDeleted()
            }
        }
    }
    EditScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack, modifier = modifier)
}

@Composable
fun EditScreen(
    state: EditState,
    onIntent: (EditIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors

    Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(
                title = stringResource(R.string.edit_title),
                onBack = onBack,
                backLabel = stringResource(R.string.a11y_back_to_counter),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = Spacing.space4, vertical = Spacing.space6),
                verticalArrangement = Arrangement.spacedBy(Spacing.space6),
            ) {
                TapTapTextField(
                    value = state.name,
                    onValueChange = { onIntent(EditIntent.ChangeName(it)) },
                    label = stringResource(R.string.edit_name),
                    required = true,
                )

                if (!state.isQuit) {
                    TapTapTextField(
                        value = state.notes,
                        onValueChange = { onIntent(EditIntent.ChangeNotes(it)) },
                        label = stringResource(R.string.field_notes),
                        optionalLabel = stringResource(R.string.label_optional),
                        singleLine = false,
                        minHeight = 88.dp,
                    )
                }

                BackgroundSection(state, onIntent)
                ColorSection(state, onIntent)

                // FR-5.8: the quit variant replaces start, step and target.
                if (state.isQuit) QuitFields(state, onIntent) else NormalFields(state, onIntent)

                SettingGroup {
                    SwitchRow(
                        label = stringResource(R.string.field_pin_to_top),
                        help = stringResource(
                            if (state.isQuit) R.string.edit_pin_help_quit else R.string.edit_pin_help,
                        ),
                        checked = state.isPinned,
                        onCheckedChange = { onIntent(EditIntent.TogglePin(it)) },
                    )
                }

                TapTapButton(
                    text = stringResource(R.string.action_save_changes),
                    onClick = { onIntent(EditIntent.Save) },
                    variant = ButtonVariant.Primary,
                    enabled = state.canSave,
                    minHeight = 56.dp,
                    modifier = Modifier.fillMaxWidth(),
                )

                // FR-5.7: delete asks first. The canvas has no sheet here; this adds one.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = false,
                            role = Role.Button,
                            onClick = { onIntent(EditIntent.DeleteClicked) },
                        )
                        .padding(vertical = Spacing.space3),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.edit_delete),
                        style = TapTapType.title,
                        color = c.danger,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(Spacing.space2).navigationBarsPadding())
            }
        }

        if (state.showDeleteDialog) {
            ConfirmSheet(
                title = stringResource(R.string.delete_title),
                message = stringResource(R.string.delete_message, state.name),
                confirmLabel = stringResource(R.string.delete_confirm),
                cancelLabel = stringResource(R.string.action_cancel),
                onConfirm = { onIntent(EditIntent.DeleteConfirmed) },
                onDismiss = { onIntent(EditIntent.DeleteDismissed) },
            )
        }
    }
}

// --- Background and colour -------------------------------------------------------------

@Composable
private fun BackgroundSection(state: EditState, onIntent: (EditIntent) -> Unit) {
    val c = TapTapTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        Text(
            text = stringResource(R.string.edit_background),
            style = TapTapType.label,
            color = c.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            Background.entries.forEach { background ->
                val selected = state.background == background
                val tileLabel = stringResource(background.labelRes())
                Box(
                    modifier = Modifier
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onIntent(EditIntent.ChangeBackground(background)) },
                        )
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) c.ink else c.lineStrong,
                            shape = TapTapShapes.md,
                        )
                        .padding(3.dp)
                        .semantics { contentDescription = tileLabel },
                ) {
                    Box(Modifier.size(64.dp).background(c.surfaceSunken, TapTapShapes.sm)) {
                        BackgroundTile(background, Modifier.size(64.dp))
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.edit_background_help),
            style = TapTapType.caption,
            color = c.inkMuted,
        )
    }
}

private fun Background.labelRes(): Int = when (this) {
    Background.NONE -> R.string.background_none
    Background.WAVES -> R.string.background_waves
    Background.ORBS -> R.string.background_orbs
    Background.HILLS -> R.string.background_hills
}

@Composable
private fun ColorSection(state: EditState, onIntent: (EditIntent) -> Unit) {
    val c = TapTapTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        Text(
            text = stringResource(R.string.edit_counter_color),
            style = TapTapType.label,
            color = c.ink,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            // FR-5.3: the first option is "same as app".
            ColorSwatch(
                name = stringResource(R.string.color_app),
                fill = null,
                onFill = null,
                selected = state.color == null,
                onClick = { onIntent(EditIntent.ChangeColor(null)) },
                appOptionLabel = stringResource(R.string.color_app),
            )
            ThemeColor.entries.forEach { color ->
                ColorSwatch(
                    name = stringResource(color.labelRes()),
                    fill = c.swatch(color),
                    onFill = Color.White,
                    selected = state.color == color,
                    onClick = { onIntent(EditIntent.ChangeColor(color)) },
                )
            }
        }
        Text(
            text = stringResource(R.string.edit_counter_color_help),
            style = TapTapType.caption,
            color = c.inkMuted,
        )
    }
}

private fun ThemeColor.labelRes(): Int = when (this) {
    ThemeColor.PINK -> R.string.color_pink
    ThemeColor.TEAL -> R.string.color_teal
    ThemeColor.SAFFRON -> R.string.color_saffron
    ThemeColor.BLUE -> R.string.color_blue
}

// --- Type-specific fields ----------------------------------------------------------------

@Composable
private fun NormalFields(state: EditState, onIntent: (EditIntent) -> Unit) {
    val c = TapTapTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        SettingGroup {
            Stepper(
                label = stringResource(R.string.field_start_value),
                help = stringResource(R.string.field_start_value_help),
                value = state.startValue,
                onValueChange = { onIntent(EditIntent.ChangeStartValue(it)) },
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
                onValueChange = { onIntent(EditIntent.ChangeStep(it.toInt())) },
                decreaseLabel = stringResource(R.string.a11y_lower_step_value),
                increaseLabel = stringResource(R.string.a11y_raise_step_value),
                min = 1,
                modifier = Modifier.padding(Spacing.space4),
            )
        }
        Text(
            text = stringResource(R.string.edit_values_help),
            style = TapTapType.caption,
            color = c.inkMuted,
        )

        SettingGroup {
            SwitchRow(
                label = stringResource(R.string.field_set_target),
                checked = state.targetOn,
                onCheckedChange = { onIntent(EditIntent.ToggleTarget(it)) },
            )
            if (state.targetOn) {
                SettingDivider()
                Stepper(
                    label = stringResource(R.string.field_target_value),
                    value = state.target,
                    onValueChange = { onIntent(EditIntent.ChangeTarget(it)) },
                    decreaseLabel = stringResource(R.string.a11y_less),
                    increaseLabel = stringResource(R.string.a11y_more),
                    min = 1,
                    modifier = Modifier.padding(Spacing.space4),
                )
                SettingDivider()
                SwitchRow(
                    label = stringResource(R.string.field_keep_counting),
                    checked = state.keepCountingAfterTarget,
                    onCheckedChange = { onIntent(EditIntent.ToggleKeepCounting(it)) },
                )
            }
        }

        // TT-031 decision 1: allowed, but say what a reset will do.
        if (state.startAboveTarget) {
            Text(
                text = stringResource(R.string.edit_start_above_target),
                style = TapTapType.caption,
                color = c.danger,
            )
        }
    }
}

@Composable
private fun QuitFields(state: EditState, onIntent: (EditIntent) -> Unit) {
    val context = LocalContext.current
    val locale = currentLocale()
    val notificationPermission = rememberDailyMessagePermission()

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space6)) {
        SettingGroup {
            ValueRow(
                label = stringResource(R.string.edit_quit_date),
                value = state.quitAtMillis?.let { Formatters.date(it, locale) },
                showChevron = true,
                onClick = {
                    showDatePicker(context, state.quitAtMillis) {
                        onIntent(EditIntent.ChangeQuitDate(it))
                    }
                },
            )
        }

        TapTapTextField(
            value = state.moneyPerDay,
            onValueChange = { onIntent(EditIntent.ChangeMoneyPerDay(it)) },
            label = stringResource(R.string.create_money_per_day),
            optionalLabel = stringResource(R.string.label_optional),
            placeholder = stringResource(R.string.create_money_hint),
            help = stringResource(R.string.create_money_help),
            keyboardType = KeyboardType.Decimal,
            prefix = currencySymbol(),
        )

        SettingGroup {
            SwitchRow(
                label = stringResource(R.string.field_set_goal),
                checked = state.goalOn,
                onCheckedChange = { onIntent(EditIntent.ToggleGoal(it)) },
            )
            if (state.goalOn) {
                SettingDivider()
                Stepper(
                    label = stringResource(R.string.field_days_to_stay_free),
                    value = state.goalDays.toLong(),
                    onValueChange = { onIntent(EditIntent.ChangeGoalDays(it.toInt())) },
                    decreaseLabel = stringResource(R.string.a11y_less),
                    increaseLabel = stringResource(R.string.a11y_more),
                    min = 1,
                    modifier = Modifier.padding(Spacing.space4),
                )
            }
        }

        TapTapTextField(
            value = state.reason,
            onValueChange = { onIntent(EditIntent.ChangeReason(it)) },
            label = stringResource(R.string.create_reason),
            optionalLabel = stringResource(R.string.label_optional),
            placeholder = stringResource(R.string.create_reason_hint),
            singleLine = false,
            minHeight = 88.dp,
        )

        SettingGroup(heading = stringResource(R.string.create_motivation)) {
            SwitchRow(
                label = stringResource(R.string.field_badges),
                help = stringResource(R.string.field_badges_help),
                checked = state.badgesOn,
                onCheckedChange = { onIntent(EditIntent.ToggleBadges(it)) },
            )
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_daily_message),
                help = stringResource(R.string.field_daily_message_help),
                checked = state.dailyMessageOn,
                // FR-8.6: ask for notifications here, and leave the switch off if refused.
                onCheckedChange = { wantOn ->
                    notificationPermission.request(wantOn) { allowed ->
                        onIntent(EditIntent.ToggleDailyMessage(allowed))
                    }
                },
            )
            SettingDivider()
            SwitchRow(
                label = stringResource(R.string.field_make_focus),
                help = stringResource(R.string.field_make_focus_help),
                checked = state.isFocus,
                onCheckedChange = { onIntent(EditIntent.ToggleFocus(it)) },
            )
        }
    }
}

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
    ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
}

// --- Previews ---------------------------------------------------------------------------

@Preview(name = "Edit · normal", heightDp = 1600)
@Composable
private fun EditNormalPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        EditScreen(
            EditState(
                isLoading = false, type = CounterType.NORMAL, name = "Push-ups",
                notes = "Sets of 10 after the morning walk.", color = ThemeColor.TEAL,
                background = Background.WAVES, targetOn = true, target = 50,
            ),
            {}, {},
        )
    }
}

@Preview(name = "Edit · quit (dark)", heightDp = 1700)
@Composable
private fun EditQuitPreview() {
    TapTapTheme(ThemeColor.BLUE, ThemeMode.DARK) {
        EditScreen(
            EditState(
                isLoading = false, type = CounterType.QUIT, name = "Quit cigarettes",
                quitAtMillis = 1_760_000_000_000L, moneyPerDay = "150", goalOn = true,
                reason = "To breathe better on my morning walk.", isFocus = true,
            ),
            {}, {},
        )
    }
}

@Preview(name = "Edit · start above target warning")
@Composable
private fun EditWarningPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        EditScreen(
            EditState(isLoading = false, name = "Countdown", startValue = 80, targetOn = true, target = 50),
            {}, {},
        )
    }
}
