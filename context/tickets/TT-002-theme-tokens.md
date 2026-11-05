# TT-002 · Colour tokens and the 8 app themes

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | L |
| Depends on | TT-001 |

## Context

Requirements §4 defines an app theme as one colour × one mode, giving 8 themes, and
`FR-7.4` forbids hard-coded colours anywhere. Every colour the app will ever draw is
already fixed in the design system's `project/tokens.json`. This ticket copies those
values into Kotlin once and exposes them the way the tech spec §9 describes.

## Scope

**In:**
- `TapTapColors` data class with the 24 tokens from `tokens.json`:
  `bg, surface, surfaceRaised, surfaceSunken, ink, inkMuted, line, lineStrong,
  accent, onAccent, accentStrong, accentSoft, onAccentSoft, danger, onDanger,
  success, onSuccess, scrim, focus, dim, swatchPink, swatchTeal, swatchSaffron, swatchBlue`.
- Eight `val` palettes: `pinkLight, pinkDark, tealLight, tealDark, saffronLight,
  saffronDark, blueLight, blueDark`, values copied **verbatim** from `tokens.json`.
- `fun tapTapColors(color: ThemeColor, dark: Boolean): TapTapColors`.
- `LocalTapTapColors` (`staticCompositionLocalOf`) and `LocalIsDark`.
- `TapTapTheme(color, mode, content)` resolving `ThemeMode.SYSTEM` through
  `isSystemInDarkTheme()`, providing both locals and a `MaterialTheme` built from
  `toMaterialScheme(dark)`.
- `CounterTheme(color: ThemeColor?, content)` — swaps **only** `accent`, `onAccent`,
  `accentStrong`, `accentSoft`, `onAccentSoft` and keeps the app's mode; `null` passes
  through unchanged.
- A short accessor object so screens write `TapTapTheme.colors.accent`.

**Out:**
- Typography — TT-003. Shapes/spacing — TT-004.
- Per-screen use of the tokens — Epics C and E.

## Acceptance criteria

- [ ] All 8 colour × mode combinations resolve to the exact hex/rgba values in
      `tokens.json`; nothing is computed, interpolated or approximated — `FR-7.1`, `FR-7.4`.
- [ ] `ThemeMode.SYSTEM` follows the phone and recomposes when the phone setting
      changes — `FR-7.2`.
- [ ] `CounterTheme` changes only the five accent tokens; `bg`, `surface`, `ink` and the
      rest stay as the app's mode set them — `FR-7.3`.
- [ ] `focus` resolves to the same value as `ink` (it is an alias in `tokens.json`).
- [ ] `swatch-*` tokens are the same in every colour theme and only follow light/dark.
- [ ] No `Color(0xFF...)` literal exists outside this file.

## Design references

- `project/tokens.json` → `color.tokens` (24 entries) and `color.themes` (8 ids).
- `project/README.md` → "Color": page = `bg`, cards = `surface` + 1px `line` border,
  sheets = `surface-raised`, inputs/tracks/unselected fills = `surface-sunken`;
  `accent-strong` when the accent is text or an icon on `bg`/`surface`; `danger` only for
  Reset, Delete and "I slipped"; `success` only for "Target reached".
- Each token's `usage` string in `tokens.json` is the rule for where it may be used —
  carry it over as a KDoc line.

## Files

- `app/src/main/java/com/habitTap/ui/theme/Color.kt`
- `app/src/main/java/com/habitTap/ui/theme/Theme.kt`
- `app/src/main/java/com/habitTap/ui/theme/CounterTheme.kt`

## Verification

A `@Preview` grid showing one card in all 8 themes. Spot-check three tokens against
`tokens.json` by hand. `grep -rn "Color(0xFF" app/src/main/java --include=*.kt` returns
hits only in `Color.kt`.
