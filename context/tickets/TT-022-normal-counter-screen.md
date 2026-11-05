# TT-022 · Normal counter screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | M |
| Depends on | TT-009, TT-012, TT-014, TT-016, TT-017, TT-019, TT-026 |

## Context

The screen the app exists for. Everything here serves one goal: a tap must register
instantly and never be lost (`NFR-2`, `NFR-3`), even when the user is not looking at the
phone.

## Scope

**In:** `CounterContract.kt`, `CounterViewModel.kt`, `CounterScreen.kt` — the contract in
spec §4.2 is already written out and should be followed as given.

Layout: `TopBar` (back, the counter name with its note as a subtitle, an edit button) →
`StatStrip` → `CounterDial` filling the remaining space → `ControlBar`, with the reset
`ConfirmSheet` on top and `CounterBackground` + scrim behind everything.

- Stat strip: **Today** (signed, e.g. `+12`), **Left to target** (`max(0, target − count)`),
  **Done** (`round(count ÷ target × 100)%`). With no target the last two show `—`.
- "Today" comes from `observeTodayChange(id)`, summing `TAP` deltas since local midnight.
- Each tap emits a `TapFeedback` effect that the Route turns into vibration + click.
- The dial state is derived from the model's `canTap` / `isTargetReached`, not recomputed.
- Reset opens the sheet; confirming calls `repository.reset(id)`.
- The counter's colour wraps the screen in `CounterTheme`; the background and scrim come
  from `Background`.

**Out:** the dial and control bar visuals (TT-012, TT-014), backgrounds (TT-017),
haptics/sound implementation (TT-026).

## Acceptance criteria

- [ ] Top bar shows back, the counter name with its note, and an edit button — `FR-3.1`.
- [ ] The stat strip shows today's change, left to target and percent done — `FR-3.2`.
- [ ] The whole dial is one tap button; each tap adds the step; the ring is count ÷ target — `FR-3.3`.
- [ ] Locked: taps do nothing, the dial greys out, the label says "Locked" — `FR-3.4`.
- [ ] Reverse: each tap subtracts the step and the count never goes below 0 — `FR-3.5`.
- [ ] Reset asks first, then sets the count to the **start value** — `FR-3.6`.
- [ ] At target with keep-counting off taps stop and the dial says "Target reached"; with
      it on, counting continues — `FR-3.7`.
- [ ] Each tap can vibrate and click, following the Settings switches — `FR-3.8`.
- [ ] The background image and counter colour from Edit apply here, with a scrim keeping
      the number readable — `FR-3.9`.
- [ ] A tap updates the number in under 50 ms and a fast burst loses none — `NFR-2`.
- [ ] Killing the app immediately after a tap keeps the count — `NFR-3`.
- [ ] "Today" resets at local midnight — `TT-031` decision 2.

## Design references

`project/Counter.dc.html` (artboard "Normal counter", 390×844) and
`project/CounterDark.dc.html` (teal dark with the waves background). The `renderVals`
block gives the stat labels, the per-state colours and the reset sheet copy.

## Files

- `app/src/main/java/com/habitTap/ui/counter/CounterContract.kt`
- `app/src/main/java/com/habitTap/ui/counter/CounterViewModel.kt`
- `app/src/main/java/com/habitTap/ui/counter/CounterScreen.kt`

## Verification

`@Preview` light/dark, plus: locked, target reached, reverse, and one with a background.
Manual: 50 fast taps land 50 increments; force-stop right after a tap and reopen — the
count is there. A ViewModel test for `Tap` while locked and for the reset dialog flow.
