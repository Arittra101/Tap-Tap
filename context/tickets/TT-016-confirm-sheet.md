# TT-016 · ConfirmSheet

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | S |
| Depends on | TT-002, TT-003, TT-004, TT-011 |

## Context

Three actions lose data and must ask first: Reset (`FR-3.6`), "I slipped" (`FR-4.8`) and
Delete counter (`FR-5.7`). The design system gives them one shape, and the brand book is
specific that a slip is not a failure — the words matter as much as the layout.

## Scope

**In:**
- A bottom sheet on a `dim` backdrop: `surfaceRaised`, `radius-xl` top corners,
  `shadow-sheet`, 24dp padding.
- Content: a title that is a **question**, one sentence saying exactly what happens, then
  a two-column row of `secondary` Cancel and a `danger` confirm whose label is a verb.
- Call sites supply the copy; the three in v1 are:
  | Use | Title | Sentence | Confirm |
  | --- | --- | --- | --- |
  | Reset | `Reset this counter?` | `The count goes back to the start value ({n}). You can't undo this.` | `Reset` |
  | Slip | `Did you slip?` | `That is okay. Your days free start again from today. Your best streak, badges and urges beaten are kept.` | `Start again` |
  | Delete | `Delete this counter?` | `{name} and its history are removed. You can't undo this.` | `Delete` |
- Dismiss on backdrop tap and on back; both map to Cancel.

**Out:** wiring to the screens — TT-022, TT-023, TT-024.

## Acceptance criteria

- [ ] Reset, slip and delete each ask before acting — `FR-3.6`, `FR-4.8`, `FR-5.7`.
- [ ] The slip wording carries no blame and says what is kept — Sheet README, design
      system README ("Be kind about quitting").
- [ ] The confirm button is a verb — design system README ("Buttons are verbs").
- [ ] Back and a backdrop tap cancel without acting.
- [ ] The sheet announces itself as a dialog to accessibility services — `NFR-4`.

## Design references

`project/components/Sheet/README.md`; `project/Counter.dc.html` → the `confirmReset`
block; `project/Quit.dc.html` → the `confirmSlip` block (both hold the exact copy above).

## Files

- `app/src/main/java/com/habitTap/ui/components/ConfirmSheet.kt`

## Verification

`@Preview` of all three sheets in light and dark. Manual: open the reset sheet, press
back — nothing is reset.
