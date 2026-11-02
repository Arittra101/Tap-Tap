# TT-015 · Form components

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | L |
| Depends on | TT-002, TT-003, TT-004, TT-018 |

## Context

New counter, Edit counter and Settings are all built from the same seven form parts
(design system README, "Screens and where components go"). Building them once is what
keeps those three long screens short.

## Scope

**In:**
- **`TapTapTextField`** — 48dp, `surfaceSunken` fill, `lineStrong` border, focus adds an
  `accentStrong` ring. Label above; required fields get a `danger` asterisk, optional ones
  say "Optional" in `inkMuted`; optional help line below. A multi-line variant at 80–96dp
  for notes and the quit reason. A prefix slot for the currency symbol.
- **`Stepper`** — label, optional help line, the value in the display font between `−`
  and `+` buttons of 44dp each, with a `min`. Long-press repeats.
- **`SettingRow`** — 64dp rows inside one `surface` group split by `line`. Optional
  leading icon, label + help text, and a trailing switch, value or chevron.
  Switch on = `accent` track with an `onAccent` knob; off = `surfaceSunken` track with a
  2px `lineStrong` outline.
- **`SegmentedControl`** — `surfaceSunken` pill, 2–3 equal options, 4dp padding; the
  selected option is a raised `surface` pill with a `lineStrong` outline.
- **`Chip`** / **`ChipRow`** — 40dp pills, optional leading colour dot. Selected =
  `accentSoft` fill, `accentStrong` border, `onAccentSoft` text. `ChipRow` **wraps** to
  several lines and never scrolls sideways in a form.
- **`TypeCard`** — two large cards side by side, ≥150dp, with an icon box, a title and a
  subtitle. Selected = `accentSoft` fill, 2px `accentStrong` border, icon box in `accent`.
- **`ColorSwatch`** — 52dp ring with the colour inside; selected = `ink` ring + a check.
  Uses `swatchPink/Teal/Saffron/Blue`. An "App" variant for Edit's "same as app" option.
  **The colour name is always shown under the swatch.**
- **`SettingGroup`** — the `surface` + `radius-lg` + `line`-divided container these rows
  and swatches sit in, with an optional `label`-styled section heading in `inkMuted`.

**Out:** the forms themselves — TT-020, TT-024, TT-025.

## Acceptance criteria

- [ ] Required fields show a `danger` asterisk; optional fields say "Optional" — TextField README.
- [ ] A `Stepper` enforces its minimum (start 0, step 1, target 1) and long-press repeats — Stepper README.
- [ ] Switches read as switches to accessibility services and the off track keeps a 3:1
      outline so it is visible without colour — `NFR-4`, `NFR-5`, SettingRow README.
- [ ] `ChipRow` wraps; it never becomes a horizontal scroller inside a form — Chip README.
- [ ] A `ColorSwatch` always shows the colour's name, so colour is never the only
      signal — `NFR-6`, ColorSwatch README.
- [ ] Every control is ≥48dp high, except the deliberately 44dp stepper buttons which keep
      a 48dp touch area — `NFR-4`.
- [ ] Rows and fields keep their text at 200% font scale — `NFR-7`.

## Design references

`project/components/TextField/README.md`, `Stepper/README.md`, `SettingRow/README.md`,
`SegmentedControl/README.md`, `Chip/README.md`, `TypeCard/README.md`,
`ColorSwatch/README.md`; `project/Create.dc.html` (the `chip`, `seg`, `typeCard`,
`typeIcon` and `ttSwitch` style helpers), `project/Settings.dc.html`, `project/Edit.dc.html`.

## Verification

One `@Preview` per component, each in light and dark and in a selected/unselected or
on/off pair. A combined preview of a `SettingGroup` with three rows at 200% font scale.
