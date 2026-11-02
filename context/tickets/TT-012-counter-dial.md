# TT-012 · CounterDial

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | L |
| Depends on | TT-002, TT-003, TT-004 |

## Context

The dial is the product. `FR-3.3` makes the whole circle one tap target so people can
count without looking, and `NFR-2` requires the number to move in under 50 ms. It also
carries four visual states that must each say their state in words, not just colour
(`NFR-6`).

## Scope

**In:**
- A 310dp circle that is **one** button. Ring: 18dp stroke, `accent` arc on a
  `surfaceSunken` track, progress = `count ÷ target`, starting at 12 o'clock
  (`rotate(-90°)`), round cap.
- Contents, top to bottom: the state pill, the count in `countXl`, "of {target}" in
  `inkMuted`, then the tap hint in the display font 17sp `accentStrong`.
- The four states, with the design's exact copy:
  | State | Pill | Hint | Helper line below the dial |
  | --- | --- | --- | --- |
  | Counting | `Step {n}` | `Tap to +{n}` | `Tap anywhere on the circle` |
  | Reverse | `Counting down` + down-arrow icon | `Tap to −{n}` | `Reverse is on. Each tap takes {n} away.` |
  | Locked | `Locked` + lock icon, `surfaceSunken` | `Locked` | `Unlock to count again. Locking stops taps by mistake.` |
  | Target reached | `Target reached` + check, `success` | `Done for today` | `Turn on "Keep counting after target" in Edit to go on.` |
- Locked styling: face `surfaceSunken`, arc `lineStrong`, number `inkMuted`, taps ignored.
  Target reached: arc `success`, number `accentStrong`.
- Press feedback: scale to 97% for 90 ms, no bounce.
- `aria`-equivalent: the button's content description is `Add {n}` / `Subtract {n}` /
  `Counter is locked` / `Target reached`; the count region is a live region so TalkBack
  announces each change.
- No target: the ring is not drawn as progress (or is drawn full in `accentSoft`) and the
  "of N" line is omitted.

**Out:** the stat strip above it and the control bar below (TT-014); the background
image behind it (TT-017); the reset sheet (TT-016).

## Acceptance criteria

- [ ] The entire circle is the tap target, not just the number — `FR-3.3`.
- [ ] Each tap applies the step value and the ring shows `count ÷ target` — `FR-3.3`.
- [ ] Locked: taps do nothing, the dial turns grey, the label says "Locked" — `FR-3.4`, `NFR-6`.
- [ ] Reverse: each tap subtracts the step and the count never shows below 0 — `FR-3.5`.
- [ ] At target with keep-counting off, taps stop and the dial says "Target reached" — `FR-3.7`, `NFR-6`.
- [ ] A tap repaints the number in under 50 ms and a fast burst drops none — `NFR-2`.
- [ ] At 200% font scale the count and hint still fit inside the ring — `NFR-7`.
- [ ] The number uses tabular figures and does not shift width — `FR-7.5`.

## Design references

`project/components/CounterDial/README.md`; `project/Counter.dc.html` — the `renderVals`
block holds the exact pill/hint/helper strings and the colour choices per state;
`project/CounterDark.dc.html` for teal-dark over a background.

## Files

- `app/src/main/java/com/taptap/ui/components/CounterDial.kt`

## Verification

A `@Preview` matrix: 4 states × light/dark, plus one at 200% font scale and one with no
target. Manual: hold the dial and tap fast for five seconds — the count equals the tap
count.
