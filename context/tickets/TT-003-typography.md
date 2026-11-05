# TT-003 · Typography and fonts

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | M |
| Depends on | TT-001 |

## Context

`FR-7.5` names two families and requires tabular figures, because the big count must not
jump width while the user taps. The design system fixes the whole scale in
`tokens.json → type`.

## Scope

**In:**
- Font families: **Outfit** (display — numbers, app name, screen titles) and
  **Noto Sans** (text), with **Noto Sans Bengali** and **Noto Sans Devanagari** in the
  stack so Bangla and Hindi render. Loaded as downloadable Google Fonts via
  `ui-text-google-fonts`, with a bundled `res/font` fallback so a first launch offline
  is not blank.
- The named styles, exactly as `tokens.json` gives them:
  | Style | Size / line / weight | Extra |
  | --- | --- | --- |
  | `countXl` | 104 / 108 / 600 | `letterSpacing -3px`, display, tabular |
  | `countLg` | 72 / 72 / 600 | `-2px`, display, tabular |
  | `stat` | 34 / 38 / 600 | display, tabular |
  | `titleLg` | 22 / 28 / 600 | display |
  | `screenTitle` | 20 / 26–28 / 600 | display — top-bar titles on inner screens |
  | `title` | 17 / 24 / 600 | text |
  | `body` | 15 / 22 / 400 | text |
  | `label` | 13 / 18 / 600 | `letterSpacing 0.2px`, text |
  | `caption` | 12 / 16 / 400 | text |
- `fontFeatureSettings = "tnum"` on every style that shows a number.
- A `TapTapTypography` mapped onto `MaterialTheme.typography` so Material 3 components
  inherit it.

**Out:**
- Localised digit shaping (Bangla digits) — TT-027.
- Per-screen use — Epics C and E.

## Acceptance criteria

- [ ] Outfit is used for numbers and titles, Noto Sans for everything else — `FR-7.5`.
- [ ] Numbers use tabular figures: the dial's width does not change going 11 → 88 — `FR-7.5`.
- [ ] Bangla and Devanagari text renders without tofu in all nine styles.
- [ ] Sizes come from `tokens.json`; no literal `sp` value sits in a screen file.
- [ ] Styles scale with the system font setting (no hard `TextUnit` override that blocks it) — `NFR-7`.

## Design references

- `project/tokens.json` → `type.families`, `type.groups` (Numbers, Text).
- `project/README.md` → "Type": `count-xl` is the dial number, `count-lg` the Focus card
  days, `stat` card counts; screen titles are 20px in the display family.

## Files

- `app/src/main/java/com/habitTap/ui/theme/Type.kt`
- `app/src/main/res/font/` (fallback faces)
- `app/src/main/res/values/font_certs.xml` if downloadable fonts need it

## Verification

A `@Preview` of all nine styles with Latin, Bangla and Devanagari samples, in light and
dark. A second preview at 200% font scale with no clipped text.
