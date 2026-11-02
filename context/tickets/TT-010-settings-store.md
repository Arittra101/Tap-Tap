# TT-010 · SettingsStore and SettingsRepository

| | |
| --- | --- |
| Epic | B · Data |
| Type | feature |
| Size | S |
| Depends on | TT-001, TT-005 |

## Context

Five preferences drive the whole app's appearance and feedback (`FR-6.1`–`FR-6.4`).
Spec §7.4 puts them in DataStore Preferences, read as one `Flow<AppSettings>`.

## Scope

**In:**
- `SettingsStore(context)` — DataStore Preferences with these keys and defaults:
  | Key | Type | Default |
  | --- | --- | --- |
  | `theme_color` | String (`ThemeColor`) | `PINK` |
  | `theme_mode` | String (`ThemeMode`) | `SYSTEM` |
  | `language` | String (BCP-47 tag) | empty = phone language if supported, else English |
  | `vibration` | Boolean | `true` |
  | `sound` | Boolean | `false` |
- `SettingsRepository` — `val settings: Flow<AppSettings>` plus one suspend setter per
  key. Unknown or corrupt stored values fall back to the default rather than throwing.

**Out:**
- Applying the locale (`AppCompatDelegate.setApplicationLocales`) — TT-027.
- Reading the settings for haptics/sound — TT-026.

## Acceptance criteria

- [ ] Defaults are exactly those in requirements §6 (Settings table) and spec §7.4.
- [ ] An empty `language` resolves to the phone language when it is one of the six,
      else English — requirements §6.
- [ ] `settings` emits on every write, so the theme updates without a restart — `FR-6.1`, `FR-6.2`.
- [ ] A value that no longer parses (e.g. a removed enum name) falls back to the default
      instead of crashing.

## Design references

`project/Settings.dc.html` → the five controls and their initial state
(`color: null` meaning "app default", `modeChoice: 'system'`, `lang: 'en'`,
`vibration: true`, `sound: false`).

## Files

- `app/src/main/java/com/taptap/data/settings/SettingsStore.kt`
- `app/src/main/java/com/taptap/data/repository/SettingsRepository.kt`

## Verification

A test with a temp-file DataStore: read defaults on an empty store, write each key, read
it back, and write a bogus `theme_color` to confirm the fallback.
