# TT-013 · CounterCard, QuitCard, FocusCard, FocusPicker

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | L |
| Depends on | TT-002, TT-003, TT-004, TT-011 |

## Context

Home shows the most important thing first (requirements §1), and these four cards are how
that ordering is expressed: one Focus card, then the counters row, then the quit list.
`FR-7.3` makes a counter's own colour accent-only, which is why each card is wrapped in
`CounterTheme` rather than being drawn in a different palette.

## Scope

**In:**
- **`CounterCard`** — 164dp wide, `radius-lg`, `surface` frame with a `line` border.
  Name (tappable, opens the counter), pin `IconButton(plain)`, the count in `stat` using
  the counter's `accentStrong`, "/ {target}" or "no target" in `inkMuted`, a 6dp progress
  bar, a meta line (`Step {n}` or `Target reached`), and a 44dp `+` button in the
  counter's `accent`. At target with keep-counting off, the `+` becomes a `success` check
  with the content description "Target reached".
  Pinned: `lineStrong` border and a filled pin.
- **`QuitCard`** — `radius-lg`, 10dp colour dot, name, make-focus `IconButton`, pin
  `IconButton`, days free in 36sp display using the counter's `accentStrong`, a right-hand
  line showing money saved **or** "{n} urges beaten today" when no money is set, a 6dp
  progress bar toward the **next badge**, and the next-badge caption.
- **`FocusCard`** — `radius-xl`, filled with the counter's `accent`, all text `onAccent`.
  Focus icon + name, a chevron that opens the quit screen, days free in `countLg`,
  a progress track in `accentStrong` with an `onAccent` fill, the next-milestone sentence,
  two bordered cells (money saved — "—" when unset, urges beaten today), the
  "I beat an urge" button in `onAccent`/`accentStrong`, and the reason in italics.
  **One action only** — no second button on the card.
- **`FocusPicker`** — `radius-xl`, `surface` with a 2px dashed `lineStrong` border.
  56dp focus icon in `accentSoft`, the heading "Pick one habit to focus on", the help line
  "It stays big at the top of Home, so you see your progress first every day.", then one
  wrapping chip per quit counter with its colour dot.
- A dashed 120dp "New counter" card to end the counters row.

**Out:** the sections, headers and list assembly — TT-021. Chip itself — TT-015.

## Acceptance criteria

- [ ] Only the count, progress fill and `+` take the counter's own colour; the card frame
      stays the app's `surface` — `FR-7.3`, CounterCard README.
- [ ] A normal card shows name, count, target or "no target", a progress bar, a pin and a
      `+` that adds the step — `FR-1.6`.
- [ ] At target with keep-counting off the `+` is a check — `FR-1.6`, `NFR-6`.
- [ ] A quit card shows name, colour dot, days free, money saved **or** urges beaten today
      when no money is set, next-badge progress, make-focus and pin — `FR-1.8`.
- [ ] The quit progress bar measures days toward the next badge, not toward the goal —
      QuitCard README.
- [ ] The Focus card shows name, days free, next-badge progress, money saved, urges today,
      the reason and "I beat an urge" — `FR-1.2`.
- [ ] The Focus card carries exactly one button — FocusCard README ("Don't").
- [ ] Tapping a chip in `FocusPicker` makes that counter the focus — `FR-1.3`.
- [ ] Pinned cards render the pinned treatment; ordering is the caller's job — `FR-1.5`, `FR-1.7`.
- [ ] Tapping a card's name opens that counter's screen — `FR-1.11`.
- [ ] Pin and focus buttons have content descriptions that change with state
      ("Pin {name}" / "Unpin {name}", "Make {name} your focus") — `NFR-4`.

## Design references

`project/components/CounterCard/README.md`, `QuitCard/README.md`, `FocusCard/README.md`,
`FocusPicker/README.md`; `project/Main.dc.html` — the `normals` / `quitAll` / `f` blocks
give the exact geometry, the `sideText` rule (money when `perDay > 0`, else urges) and the
"—" fallback.

## Files

- `app/src/main/java/com/habitTap/ui/components/CounterCard.kt`
- `app/src/main/java/com/habitTap/ui/components/QuitCard.kt`
- `app/src/main/java/com/habitTap/ui/components/FocusCard.kt`
- `app/src/main/java/com/habitTap/ui/components/FocusPicker.kt`

## Verification

`@Preview` per card in light and dark, each with a counter colour different from the app
colour, plus: a normal card with no target, one at target, a quit card with and without
money, and a `FocusPicker` with three counters.
