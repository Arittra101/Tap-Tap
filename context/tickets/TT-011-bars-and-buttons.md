# TT-011 · TopBar, IconButton, Button, StatusPill

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | M |
| Depends on | TT-002, TT-003, TT-004, TT-018 |

## Context

Four components every screen uses. Building them once keeps the six screens consistent
and keeps `NFR-4` (48dp targets, content descriptions everywhere) in one place instead of
six.

## Scope

**In:**
- **`TopBar`** — 64dp, **the same colour as the page** (`bg`), a 1px `line` divider under
  it, no shadow. Two shapes:
  - Home: the `tt-mark` 40dp `radius-md` square in `accent` with a plus glyph in
    `onAccent`, "HabitTap" in `titleLg`, one action on the right.
  - Inner screens: 48dp back button, centred 20dp display title with an optional
    `caption` subtitle beneath, and either one action or an empty 48dp slot so the title
    stays centred.
- **`IconButton`** — two variants: framed (48dp, `surface` fill, `line` border — top bar)
  and `plain` (40dp, no fill — inside cards). `pressed = true` turns the icon
  `accentStrong` and fills it; that is the pin state.
- **`Button`** — 52dp high, `radius-md`, `label` weight 600 at 15sp, optional leading
  icon, variants:
  | Variant | Fill / content |
  | --- | --- |
  | `primary` | `accent` / `onAccent` — one per screen |
  | `secondary` | `surface` / `ink` with a `lineStrong` border |
  | `danger` | `danger` / `onDanger` — only the confirm button inside a sheet |
  | `dangerOutline` | `surface` / `danger` with a `lineStrong` border — the "I slipped" entry |
  | `fab` | `ink` fill, `bg` content, 56dp pill with a shadow — Home only |
  | disabled | `surfaceSunken` / `inkMuted` |
- **`StatusPill`** — 26dp pill, a word plus an icon when the state matters: default
  `accentSoft`, Locked `surfaceSunken`, Target reached `success` + check.

**Out:** the dial (TT-012), cards (TT-013), form controls (TT-015).

## Acceptance criteria

- [ ] Every icon-only button takes a required, non-null content description — `NFR-4`.
- [ ] Touch targets are ≥48dp (the 40dp `plain` variant keeps a 48dp touch area) — `NFR-4`.
- [ ] `TopBar` is never painted `surface` and never carries a shadow; only the divider
      separates it — TopBar README ("Don't").
- [ ] At most one action sits on the right of a `TopBar` — TopBar README.
- [ ] `danger` is used only for Reset, Delete and "I slipped" — design system README.
- [ ] `StatusPill` never signals state by colour alone — `NFR-6`, StatusPill README.

## Design references

`project/components/TopBar/README.md`, `IconButton/README.md`, `Button/README.md`,
`StatusPill/README.md`. Geometry from `project/Main.dc.html` (Home bar) and
`project/Counter.dc.html` / `Quit.dc.html` / `Settings.dc.html` (inner bars).

## Files

- `app/src/main/java/com/habitTap/ui/components/TopBar.kt`
- `app/src/main/java/com/habitTap/ui/components/IconButton.kt`
- `app/src/main/java/com/habitTap/ui/components/Button.kt`
- `app/src/main/java/com/habitTap/ui/components/StatusPill.kt`

## Verification

`@Preview` in light and dark for: the Home bar, an inner bar with and without a right
action, all six button variants, and the three pill states. TalkBack reads a label for
every icon-only button.
