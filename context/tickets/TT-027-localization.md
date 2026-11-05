# TT-027 · Localisation and number formatting

| | |
| --- | --- |
| Epic | F · Platform |
| Type | feature |
| Size | L |
| Depends on | TT-001, TT-010, TT-020, TT-021, TT-022, TT-023, TT-024, TT-025 |

## Context

Six languages, switchable inside the app without touching the phone's language
(`FR-8.1`–`FR-8.3`). Scheduled last among the screens because it touches all of them: it
is far cheaper to extract strings once every screen is final than to keep six files in
step while the screens change.

## Scope

**In:**
- Extract **every** visible string to `res/values/strings.xml`, then translate into
  `values-bn`, `values-hi`, `values-ru`, `values-es`, `values-it`.
- Plurals as `<plurals>`, not string concatenation — "1 day" / "3 days",
  "{n} days to your {m}-day badge", "{n} counters", "{n} habits", "{n} of 10 earned".
- Per-app language: `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))`
  on change, persisted through TT-010's `language` key, with `locales_config.xml` from
  TT-001.
- Locale-aware numbers: digits follow the chosen language, so Bangla shows Bangla digits.
  Use `NumberFormat.getInstance(locale)` for counts and days.
- Locale-aware dates for "Free since {date}".
- **Money uses the device's local currency symbol** via
  `NumberFormat.getCurrencyInstance(locale)`.
- Keep the design's voice in the English source: short sentences, sentence case, verbs on
  buttons, no emoji, no religious words.

**Note on the design's currency.** The artboards hard-code `৳` with `en-IN` grouping
(`'৳' + n.toLocaleString('en-IN')`). That is a mockup convenience, not the rule —
`FR-8.3` says the local currency symbol, so the implementation formats by locale and the
Bengali taka appears only for users whose locale uses it.

**Out:** new copy. Every string already exists in the artboards; this ticket moves and
translates, it does not invent.

## Acceptance criteria

- [ ] No hard-coded user-visible text anywhere in `app/src/main/java` — `FR-8.1`.
- [ ] All six locales have a complete `strings.xml` with no missing keys — `FR-8.1`.
- [ ] The in-app language change takes effect immediately and does not change the phone
      language — `FR-8.2`.
- [ ] Numbers and dates follow the chosen language, including Bangla digits in Bangla — `FR-8.3`.
- [ ] Money uses the local currency symbol, not a hard-coded `৳` — `FR-8.3`.
- [ ] Counts use `<plurals>`, so "1 day" and "2 days" are both correct in every language,
      including Russian's three plural forms.
- [ ] Bangla and Hindi render in Noto Sans Bengali / Devanagari with no tofu — `FR-7.5`.
- [ ] No layout clips in the longest language (usually Russian or Spanish) — `NFR-7`.

## Design references

Every artboard is the source of English copy — take the strings verbatim.
`project/README.md` → "Content and voice" is the rule for anything that must be rephrased
for grammar.

## Files

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-{bn,hi,ru,es,it}/strings.xml`
- `app/src/main/java/com/habitTap/util/Formatters.kt` (new — number, date and money)
- `app/src/main/java/com/habitTap/ui/settings/SettingsViewModel.kt` (locale apply)

## Verification

Run the app in each of the six languages and walk all six screens. Check Bangla digits on
the dial, a Russian plural at 1 / 2 / 5 days, and the money symbol on a device set to a
non-Indian locale. Lint's `MissingTranslation` check passes.
