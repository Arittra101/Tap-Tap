# TT-024 · Edit counter screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | L |
| Depends on | TT-009, TT-015, TT-016, TT-017, TT-019, TT-020, TT-031 |

## Context

Edit is where a counter gets its own look (`FR-5.2`, `FR-5.3`) and where it can be deleted.
`FR-5.4` carries a subtle rule worth stating up front: changing the step or start value
must **never** move the current count.

**Three gaps this ticket closes.** The canvas draws only the *normal* variant of this
screen, but `FR-5.8` requires the quit fields in place of start/step/target — so the quit
variant is assembled from TT-020's quit field set, same components, same copy. And the
canvas's "Delete counter" button has no confirmation, while `FR-5.7` says it must ask —
so it gets a `ConfirmSheet`.

## Scope

**In:** `EditContract.kt`, `EditViewModel.kt`, `EditScreen.kt`.

Shared: `TopBar` (back, "Edit counter") → `Name` (required) and `Notes` → **Background
image** (None / Waves / Orbs / Hills tiles, help `Default images take the counter's color.
A soft layer keeps the number easy to read.`) → **Counter color** (an "App" swatch plus
Pink, Teal, Saffron, Blue, help `Only this counter changes color. Light or dark mode still
follows the app.`) → the type-specific block → `Pin to top` → `Save changes` → a `danger`
`Delete counter`.

**Normal block:** `Start value` (help `Reset goes back here`), `Step value` (help
`Added per tap`), the shared help line `The current count does not change. A new step
works from the next tap. A new start value is used the next time you reset.`, a
`Set a target` switch + `Target value` stepper, and `Keep counting after target`.

**Quit block (`FR-5.8`):** quit date, money per day, goal in days, reason, and the four
motivation switches — the same fields and copy as TT-020.

**Start > target warning (TT-031 decision 1):** when the start value exceeds the target,
show an inline caption under the target field —
`The start value is above the target, so a reset will already be past it.` — in `danger`.
Saving is still allowed.

Save emits a `Saved` effect; Delete opens the sheet and, on confirm, emits `Deleted` so
the Route can clear the stack back to Home.

**Out:** background drawing (TT-017), swatch/stepper components (TT-015).

## Acceptance criteria

- [ ] Name and notes are editable — `FR-5.1`.
- [ ] A background can be chosen: none, Waves, Orbs or Hills — `FR-5.2`.
- [ ] A counter colour can be chosen: "Same as app" or Pink/Teal/Saffron/Blue, and light
      or dark still follows the app — `FR-5.3`, `FR-7.3`.
- [ ] Start and step are editable; a new step applies from the next tap, a new start value
      from the next reset, and **the current count does not change** — `FR-5.4`.
- [ ] Target switch, target value and keep-counting are editable — `FR-5.5`.
- [ ] Pin to top is editable — `FR-5.6`.
- [ ] Save persists; Delete asks first and then removes the counter and its events — `FR-5.7`.
- [ ] For a quit counter the quit fields from `FR-2.3`/`FR-2.4` replace start, step and
      target — `FR-5.8`.
- [ ] Start value above the target shows the warning and still saves — `TT-031` decision 1.
- [ ] After delete the user lands on Home with no stale back entry — `FR-5.7`, TT-019.
- [ ] Changing the colour previews on the swatch row immediately.

## Design references

`project/Edit.dc.html` (artboard "Edit counter", 390×1580) — the normal variant, the four
background tiles, the five swatches including "App", and the exact help lines above.
The quit variant has no artboard: use `project/Create.dc.html`'s quit section.
`project/components/ColorSwatch/README.md` ("In Edit counter it sets the counter's own
color, with a first option 'App'").

## Files

- `app/src/main/java/com/habitTap/ui/edit/EditContract.kt`
- `app/src/main/java/com/habitTap/ui/edit/EditViewModel.kt`
- `app/src/main/java/com/habitTap/ui/edit/EditScreen.kt`

## Verification

`@Preview` of both variants in light and dark. Manual: set a counter to Teal + Waves and
open it — the dial, bar and background are teal; set the step to 5, return to the counter,
and the count is unchanged but the next tap adds 5. Delete a counter and confirm it is
gone from Home.
