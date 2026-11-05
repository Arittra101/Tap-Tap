# TT-025 · Settings screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | M |
| Depends on | TT-010, TT-015, TT-019, TT-027 |

## Context

Five settings and a privacy link (`FR-6.1`–`FR-6.5`). The one piece of real logic is the
mode note: "System" is not a theme, so the screen has to tell the user which theme they
actually ended up with.

## Scope

**In:** `SettingsContract.kt`, `SettingsViewModel.kt`, `SettingsScreen.kt`.

Layout: `TopBar` (back, "Settings", an empty right slot so the title stays centred), then
four groups with `label`-styled `inkMuted` headings:

1. **App appearance** — "Color theme" as four `ColorSwatch`es (Pink, Teal, Saffron, Blue;
   default Pink) each with its name beneath; then "Mode" as a Light / Dark / System
   `SegmentedControl` (default System); then the result note in an `accentSoft` box:
   `Phone is in dark mode, so the app uses Pink ~ Dark` when System is chosen, and
   `The app uses Pink ~ Dark` otherwise.
2. **Language** — six selectable rows, each showing the name **in its own script** with
   the English name beneath and a radio on the right:
   | Row | Native | English |
   | --- | --- | --- |
   | en | English | English |
   | bn | বাংলা | Bangla |
   | hi | हिन्दी | Hindi |
   | ru | Русский | Russian |
   | es | Español | Spanish |
   | it | Italiano | Italian |
   Each native label carries its own `lang` so the right font is picked.
3. **When you tap** — `Vibration` (help `Short buzz on every + and −`, default on) and
   `Sound` (help `Soft click on every + and −`, default off) as `SettingRow` switches.
4. **About** — a `Privacy policy` row with a shield icon and a chevron.

Choosing a language calls `AppCompatDelegate.setApplicationLocales` (TT-027).

**Out:** the locale plumbing itself — TT-027. Haptics/sound behaviour — TT-026.

## Acceptance criteria

- [ ] Four colour themes, default Pink — `FR-6.1`.
- [ ] Light / Dark / System, default System, with a note showing the resulting theme — `FR-6.2`.
- [ ] Six languages, each shown in its own script — `FR-6.3`.
- [ ] Vibration (default on) and Sound (default off) switches apply to every + and − — `FR-6.4`.
- [ ] A privacy policy link is present — `FR-6.5`.
- [ ] Changing the colour or mode repaints the app immediately, with no restart — `FR-6.1`, `FR-6.2`.
- [ ] Changing the language applies without changing the phone language — `FR-8.2`.
- [ ] Swatches show their colour name, so colour is never the only cue — `NFR-6`.
- [ ] Every row is ≥48dp and the switches read as switches to TalkBack — `NFR-4`.

## Design references

`project/Settings.dc.html` (artboard "Settings", 390×1210) and
`project/SettingsDark.dc.html` (blue dark). The `comboText` function in `renderVals` gives
the exact two forms of the mode note; the `langs` array gives the six native/English pairs.

## Files

- `app/src/main/java/com/habitTap/ui/settings/SettingsContract.kt`
- `app/src/main/java/com/habitTap/ui/settings/SettingsViewModel.kt`
- `app/src/main/java/com/habitTap/ui/settings/SettingsScreen.kt`

## Verification

`@Preview` in light and dark. Manual: switch through all four colours and all three modes
and confirm the note matches what is on screen; switch to Bangla and confirm the whole app
follows while the phone stays in English.
