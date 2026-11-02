# TT-021 · Home screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | L |
| Depends on | TT-009, TT-011, TT-013, TT-019, TT-020 |

## Context

Home decides what the user sees first every day: one Focus quit counter, then normal
counters, then the other quit counters (requirements §1). It is one screen with three
mutually exclusive top states, which is exactly how the canvas models it — one artboard
with a `variant` prop of `full` / `noFocus` / `empty`.

## Scope

**In:** `HomeContract.kt`, `HomeViewModel.kt`, `HomeScreen.kt`.

- `TopBar`: the app mark, "Tap Tap", a Settings button, and the `line` divider.
- **Focus area**, one of three:
  1. A `FocusCard` when a focus quit counter exists.
  2. A `FocusPicker` ("Your focus" / "Not set yet") when quit counters exist but none is
     focus.
  3. Nothing at all when there are no quit counters.
- **"Counters"** — section header with the count and "· pinned first", then a horizontal
  row of `CounterCard`s, pinned first, edge-to-edge with 16dp side padding, ending with
  the dashed "New counter" card.
- **"Quit tracker"** — section header, then a vertical list of `QuitCard`s for every quit
  counter **except** the focus, pinned first.
- **First run** (no counters at all): the 168dp ring illustration, "Welcome to Tap Tap",
  "Count anything you do, or quit a bad habit day by day. Start with your first counter.",
  two big choice rows (Normal counter — "Reps, glasses of water, pages, anything"; Quit
  counter — "Stay away from a bad habit and watch the days add up"), then "Popular ideas"
  chips: Push-ups, Glasses of water, Pages read, Quit smoking, Less phone at night.
  **The floating button is hidden in this state.**
- A `fab`-variant "New counter" button bottom-right otherwise.
- ViewModel: `combine(repository.observeAll(), minuteTicker)` so days free stay current
  while Home is open; `focus` = the quit counter with `isFocus`, `normalCounters` = type
  NORMAL, `quitCounters` = type QUIT minus the focus (spec §8).
- Intents: `AddTap(id)`, `BeatUrge(id)`, `TogglePin(id)`, `MakeFocus(id)`.
  `AddTap` and `BeatUrge` go through `CounterRepository`, so Home and the counter screens
  cannot disagree.

**Out:** the cards themselves — TT-013.

## Acceptance criteria

- [ ] Top bar shows the app mark, "Tap Tap", a Settings button and a thin divider — `FR-1.1`.
- [ ] A focus counter renders the Focus card first with name, days free, next-badge
      progress, money saved, urges today, the reason and "I beat an urge" — `FR-1.2`.
- [ ] Quit counters but no focus → the "Pick one habit to focus on" card with one chip per
      quit counter; tapping a chip makes it the focus — `FR-1.3`.
- [ ] No quit counters → the focus area is hidden entirely — `FR-1.4`.
- [ ] "Counters" is a horizontal row, pinned first, ending in the "New counter" card — `FR-1.5`.
- [ ] A `+` tap adds the step value and the card updates immediately — `FR-1.6`.
- [ ] "Quit tracker" lists every quit counter except the focus, pinned first — `FR-1.7`, `FR-1.8`.
- [ ] The floating button opens New counter — `FR-1.9`.
- [ ] First run shows the welcome, the two choices and the idea chips, and hides the
      floating button — `FR-1.10`.
- [ ] Tapping a card's name opens that counter's screen — `FR-1.11`.
- [ ] Days free refresh at least once a minute without a manual reload — spec §8.
- [ ] Tapping `+` on Home and tapping the dial on the counter screen give the same result
      for the same counter — spec §4.3.

## Design references

`project/Main.dc.html` (artboard "Home", 390×1240) with its three variants:
`project/HomeNoFocus.dc.html` (390×1250), `project/HomeEmpty.dc.html` (390×844), and
`project/HomeDark.dc.html` (saffron dark). The `renderVals` block gives the section
header captions ("{n} counters · pinned first", "{n} habits · pinned first"), the
`byPin()` ordering and the idea chip list.

## Files

- `app/src/main/java/com/taptap/ui/home/HomeContract.kt`
- `app/src/main/java/com/taptap/ui/home/HomeViewModel.kt`
- `app/src/main/java/com/taptap/ui/home/HomeScreen.kt`

## Verification

`@Preview` of all three states in light and dark, plus one in a non-default colour theme.
Manual: with zero counters, one quit counter, and four normal + three quit counters.
A ViewModel test that the focus counter is excluded from `quitCounters`.
