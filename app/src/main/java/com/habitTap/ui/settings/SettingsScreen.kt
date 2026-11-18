package com.habitTap.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitTap.R
import com.habitTap.model.Currencies
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.ColorSwatch
import com.habitTap.ui.components.Segment
import com.habitTap.ui.components.SegmentedControl
import com.habitTap.ui.components.SettingDivider
import com.habitTap.ui.components.SettingGroup
import com.habitTap.ui.components.SwitchRow
import com.habitTap.ui.components.TapTapIcons
import com.habitTap.ui.components.TopBar
import com.habitTap.ui.components.ValueRow
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import com.habitTap.ui.theme.swatch
import com.habitTap.util.Formatters
import com.habitTap.util.currentLocale
import com.habitTap.util.rememberDailyMessagePermission
import com.habitTap.util.rememberNotificationsEnabled
import com.habitTap.util.rememberOpenNotificationSettings
import java.util.Currency
import java.util.Locale

/** Where the privacy policy lives (FR-6.5, FR-8.7) — the published `PRIVACY.md`. */
private const val PRIVACY_POLICY_URL = "https://habit-tap-privacy-policy.vercel.app/"

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onApplyLanguage: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ApplyLanguage -> onApplyLanguage(effect.tag)
                SettingsEffect.OpenPrivacyPolicy -> {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri()),
                        )
                    }
                }
            }
        }
    }

    SettingsScreen(
        // FR-6.2: the note needs the phone's own setting, which only Compose knows.
        state = state.copy(phoneIsDark = androidx.compose.foundation.isSystemInDarkTheme()),
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors

    val locale = currentLocale()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding(),
    ) {
        TopBar(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
            backLabel = stringResource(R.string.action_back_to_home),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space4, vertical = Spacing.space6),
            verticalArrangement = Arrangement.spacedBy(Spacing.space6),
        ) {
            AppearanceSection(state, onIntent)
            LanguageSection(state, onIntent)

            NotificationSection()

            SettingGroup(heading = stringResource(R.string.settings_money)) {
                ValueRow(
                    label = stringResource(R.string.settings_currency),
                    help = stringResource(R.string.settings_currency_help),
                    // Short on purpose: "৳ BDT", not the full name. The row already
                    // carries two lines of help, and a long value squeezed them into a
                    // narrow, ragged column. The names live in the list, one tap away.
                    value = currencyShortLabel(state.currencyCode, locale),
                    showChevron = true,
                    onClick = { onIntent(SettingsIntent.CurrencyClicked) },
                )
            }

            SettingGroup(heading = stringResource(R.string.settings_when_you_tap)) {
                SwitchRow(
                    label = stringResource(R.string.settings_vibration),
                    help = stringResource(R.string.settings_vibration_help),
                    icon = TapTapIcons.Vibrate,
                    checked = state.vibration,
                    onCheckedChange = { onIntent(SettingsIntent.ToggleVibration(it)) },
                )
                SettingDivider()
                SwitchRow(
                    label = stringResource(R.string.settings_sound),
                    help = stringResource(R.string.settings_sound_help),
                    icon = TapTapIcons.Sound,
                    checked = state.sound,
                    onCheckedChange = { onIntent(SettingsIntent.ToggleSound(it)) },
                )
            }

            SettingGroup(heading = stringResource(R.string.settings_about)) {
                ValueRow(
                    label = stringResource(R.string.settings_privacy),
                    icon = TapTapIcons.Shield,
                    showChevron = true,
                    onClick = { onIntent(SettingsIntent.OpenPrivacyPolicy) },
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

    if (state.showCurrencyPicker) {
        CurrencySheet(
            selected = state.currencyCode,
            locale = locale,
            onPick = { onIntent(SettingsIntent.ChangeCurrency(it)) },
            onDismiss = { onIntent(SettingsIntent.CurrencyDismissed) },
        )
    }
}

/**
 * Turning the daily message on, and the way back from a refusal.
 *
 * Entirely platform state — there is nothing of the app's own to keep, so this asks the
 * system directly rather than going through the ViewModel. Tapping it when off asks for
 * permission, and if Android has stopped showing that dialog (two refusals and it never
 * appears again) the callback comes straight back as refused, which is the signal to open
 * the system screen where the decision can still be changed. Tapping it when on opens the
 * same screen, which is where it is turned off again.
 */
@Composable
private fun NotificationSection() {
    val enabled = rememberNotificationsEnabled()
    val openSystemSettings = rememberOpenNotificationSettings()
    val permission = rememberDailyMessagePermission()

    SettingGroup(heading = stringResource(R.string.settings_notifications)) {
        ValueRow(
            label = stringResource(R.string.settings_notifications_daily),
            help = stringResource(R.string.settings_notifications_help),
            icon = TapTapIcons.Sound,
            value = stringResource(
                if (enabled) R.string.settings_notifications_on else R.string.settings_notifications_off,
            ),
            showChevron = true,
            onClick = {
                if (enabled) {
                    openSystemSettings()
                } else {
                    permission.request(true) { granted -> if (!granted) openSystemSettings() }
                }
            },
        )
    }
}

/**
 * How a currency reads in the list: its symbol, then its name in the user's own language —
 * "₹  Indian Rupee". Both come from [java.util.Currency], so the list needs no
 * translations of its own.
 */
@Composable
private fun currencyLabel(code: String, locale: Locale): String {
    if (code == Currencies.FOLLOW_DEVICE) return stringResource(R.string.settings_currency_device)
    val symbol = Formatters.currencySymbol(locale, code)
    val name = Formatters.currencyName(code, locale).ifEmpty { code }
    return if (symbol.isEmpty() || symbol == code) name else "$symbol  $name"
}

/**
 * The same currency as the Settings row shows it: "৳ BDT".
 *
 * Short enough never to wrap, in any language, at any screen width — the row's job is to
 * answer "which currency?" at a glance, and the full name is one tap away. Following the
 * phone resolves to whatever that is, because what the money will look like is the useful
 * answer; which row is ticked is the list's business.
 */
@Composable
private fun currencyShortLabel(code: String, locale: Locale): String {
    val resolved = code.ifEmpty { runCatching { Currency.getInstance(locale).currencyCode }.getOrNull() }
        ?: return stringResource(R.string.settings_currency_device)
    val symbol = Formatters.currencySymbol(locale, resolved)
    return if (symbol.isEmpty() || symbol == resolved) resolved else "$symbol $resolved"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySheet(
    selected: String,
    locale: Locale,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = TapTapTheme.colors
    val title = stringResource(R.string.settings_currency)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = TapTapShapes.sheet,
        containerColor = c.surfaceRaised,
        contentColor = c.ink,
        scrimColor = c.dim,
        dragHandle = null,
        modifier = Modifier.semantics { paneTitle = title },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The list is longer than a sheet on a small phone, so it scrolls, and it
                // carries the bottom inset itself — a sheet does not get one for free.
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .selectableGroup(),
        ) {
            Text(
                text = title,
                style = TapTapType.title,
                color = c.ink,
                modifier = Modifier.padding(
                    start = Spacing.space4,
                    end = Spacing.space4,
                    top = Spacing.space6,
                    bottom = Spacing.space3,
                ),
            )
            // "Follow the phone" first: it is the default and what most people want.
            (listOf(Currencies.FOLLOW_DEVICE) + Currencies.codes).forEachIndexed { index, code ->
                if (index > 0) SettingDivider()
                val isSelected = code == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onPick(code) },
                        )
                        .background(if (isSelected) c.accentSoft else Color.Transparent)
                        .padding(horizontal = Spacing.space4, vertical = Spacing.space2),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                ) {
                    Text(
                        text = currencyLabel(code, locale),
                        style = TapTapType.body,
                        color = if (isSelected) c.onAccentSoft else c.ink,
                        modifier = Modifier.weight(1f),
                    )
                    RadioDot(selected = isSelected)
                }
            }
            Spacer(Modifier.height(Spacing.space6))
        }
    }
}

@Composable
private fun AppearanceSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val c = TapTapTheme.colors

    SettingGroup(heading = stringResource(R.string.settings_appearance)) {
        Column(
            modifier = Modifier.padding(Spacing.space4),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                Text(
                    text = stringResource(R.string.settings_color_theme),
                    style = TapTapType.body,
                    color = c.ink,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                ) {
                    ThemeColor.entries.forEach { color ->
                        ColorSwatch(
                            name = stringResource(color.labelRes()),
                            fill = c.swatch(color),
                            onFill = Color.White,
                            selected = state.color == color,
                            onClick = { onIntent(SettingsIntent.ChangeColor(color)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = c.line)

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                Text(
                    text = stringResource(R.string.settings_mode),
                    style = TapTapType.body,
                    color = c.ink,
                )
                SegmentedControl(
                    segments = listOf(
                        Segment(ThemeMode.LIGHT, stringResource(R.string.settings_mode_light)),
                        Segment(ThemeMode.DARK, stringResource(R.string.settings_mode_dark)),
                        Segment(ThemeMode.SYSTEM, stringResource(R.string.settings_mode_system)),
                    ),
                    selected = state.mode,
                    onSelect = { onIntent(SettingsIntent.ChangeMode(it)) },
                )
                // FR-6.2: "System" is not a theme, so say which one the user actually got.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.accentSoft, TapTapShapes.md)
                        .padding(horizontal = Spacing.space3, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                ) {
                    Icon(
                        imageVector = TapTapIcons.Focus,
                        contentDescription = null,
                        tint = c.onAccentSoft,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = modeNote(state),
                        style = TapTapType.label,
                        color = c.onAccentSoft,
                    )
                }
            }
        }
    }
}

/** The two forms of the mode note, from the canvas's `comboText`. */
@Composable
private fun modeNote(state: SettingsState): String {
    val isDark = when (state.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> state.phoneIsDark
    }
    val colorName = stringResource(state.color.labelRes())
    val modeName = stringResource(
        if (isDark) R.string.settings_mode_name_dark else R.string.settings_mode_name_light,
    )
    return if (state.mode == ThemeMode.SYSTEM) {
        val word = stringResource(
            if (state.phoneIsDark) R.string.settings_mode_word_dark else R.string.settings_mode_word_light,
        )
        stringResource(R.string.settings_mode_note_system, word, colorName, modeName)
    } else {
        stringResource(R.string.settings_mode_note_fixed, colorName, modeName)
    }
}

@Composable
private fun LanguageSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    val c = TapTapTheme.colors

    SettingGroup(heading = stringResource(R.string.settings_language)) {
        Column(Modifier.selectableGroup()) {
            Language.entries.forEachIndexed { index, language ->
                if (index > 0) SettingDivider()
                val selected = state.languageTag == language.tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onIntent(SettingsIntent.ChangeLanguage(language.tag)) },
                        )
                        .background(if (selected) c.accentSoft else Color.Transparent)
                        .padding(horizontal = Spacing.space4, vertical = Spacing.space2),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            text = stringResource(language.nativeRes),
                            style = TapTapType.body.copy(
                                // So the right font in the stack is picked for this script.
                                localeList = LocaleList(language.tag),
                            ),
                            color = if (selected) c.onAccentSoft else c.ink,
                        )
                        Text(
                            text = stringResource(language.englishRes),
                            style = TapTapType.caption,
                            color = c.inkMuted,
                        )
                    }
                    RadioDot(selected = selected)
                }
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val c = TapTapTheme.colors
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                width = 2.dp,
                color = if (selected) c.accentStrong else c.lineStrong,
                shape = TapTapShapes.pill,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(Modifier.size(10.dp).background(c.accentStrong, TapTapShapes.pill))
        }
    }
}

/** The six languages, in the order Settings lists them (FR-6.3). */
private enum class Language(val tag: String, val nativeRes: Int, val englishRes: Int) {
    EN("en", R.string.lang_native_en, R.string.lang_en),
    BN("bn", R.string.lang_native_bn, R.string.lang_bn),
    HI("hi", R.string.lang_native_hi, R.string.lang_hi),
    RU("ru", R.string.lang_native_ru, R.string.lang_ru),
    ES("es", R.string.lang_native_es, R.string.lang_es),
    IT("it", R.string.lang_native_it, R.string.lang_it),
}

private fun ThemeColor.labelRes(): Int = when (this) {
    ThemeColor.PINK -> R.string.color_pink
    ThemeColor.TEAL -> R.string.color_teal
    ThemeColor.SAFFRON -> R.string.color_saffron
    ThemeColor.BLUE -> R.string.color_blue
}

@Preview(name = "Settings · pink light", heightDp = 1250)
@Composable
private fun SettingsPreview() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) {
        SettingsScreen(SettingsState(), {}, {})
    }
}

@Preview(name = "Settings · blue dark", heightDp = 1250)
@Composable
private fun SettingsDarkPreview() {
    TapTapTheme(ThemeColor.BLUE, ThemeMode.DARK) {
        SettingsScreen(
            SettingsState(color = ThemeColor.BLUE, mode = ThemeMode.SYSTEM, phoneIsDark = true),
            {}, {},
        )
    }
}
