# TT-029 · Accessibility and font-scaling pass

| | |
| --- | --- |
| Epic | F · Platform |
| Type | chore |
| Size | M |
| Depends on | TT-021, TT-022, TT-023, TT-024, TT-025, TT-027 |

## Context

`NFR-4`–`NFR-7` are written as app-wide properties, so they cannot be signed off inside
any single screen's ticket. This is the pass that checks them across all six screens once
the text is final, and fixes what it finds.

## Scope

**In:**
- Touch targets: every interactive element ≥48dp, including the 44dp stepper and card `+`
  buttons, which keep a 48dp touch area.
- Content descriptions on every icon-only button, with state in the label where the
  button is a toggle ("Pin to top" / "Unpin", "Lock" / "Unlock").
- Contrast: verify `ink` and `inkMuted` at 4.5:1 on `bg`, `surface`, `surfaceRaised`,
  `surfaceSunken` and over the scrim in **all 8 themes**; `lineStrong` at 3:1 for control
  borders; `onAccent` on `accent` in all 8.
- No state by colour alone: target reached, locked and slips each carry a word or an icon.
- Font scaling to 200% on all six screens, with a special look at the dial (104sp),
  the Focus card (72sp) and the stat strips.
- Headings and live regions: section headers marked as headings, the dial's count as a
  live region, the sheets announced as dialogs.
- Keyboard / D-pad focus order plus a 2px `focus` ring at 2px offset.

**Out:** redesigning anything. If a check fails, the fix belongs here; if the **design**
fails a check, record it and raise it rather than improvising a new look.

## Acceptance criteria

- [ ] Every touch target is ≥48dp and every icon-only button has a content description — `NFR-4`.
- [ ] Text meets 4.5:1 in all 8 themes (3:1 for 24px+ text, control borders, focus rings) — `NFR-5`.
- [ ] Target reached, locked and slip states each show a word or icon, never colour alone — `NFR-6`.
- [ ] All six screens survive 200% font scale with no clipped or overlapping text — `NFR-7`.
- [ ] TalkBack can complete: create a counter, tap it ten times, reset it, and make a quit
      counter the focus.
- [ ] Android Studio's accessibility lint and the Compose semantics tests report no
      missing descriptions.

## Design references

`project/README.md` → the design system's own accessibility rules (focus ring 2px solid
`focus` at 2px offset; icon-only buttons always have a text label for screen readers;
`danger`/`success` never used alone). `tokens.json` → the `ink` / `ink-muted` usage notes
state they already pass 4.5:1 on the four surfaces, so a failure here means an
implementation bug, not a token problem.

## Files

Touches all of `app/src/main/java/com/habitTap/ui/` plus
`app/src/androidTest/java/com/habitTap/` for the semantics tests.

## Verification

A checklist run per screen × 8 themes with a contrast checker, a 200% font-scale pass, and
one scripted TalkBack walk-through of the four flows above.
