# TT-014 · StatStrip, ControlBar, MilestoneBadge

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | M |
| Depends on | TT-002, TT-003, TT-004, TT-011, TT-018 |

## Context

Three components shared by the two counter screens. `FR-3.2` and `FR-4.3` both ask for a
three-cell stat strip, so it is one component with different content; `FR-3.4`–`FR-3.6`
are driven by the control bar; `FR-4.6` needs the badge row.

## Scope

**In:**
- **`StatStrip`** — exactly three cells in a `surface` card with `radius-lg`, split by
  `line` dividers. Each cell: a value in the display font (22sp on the normal counter,
  20sp on the quit counter) over a `caption` label in `inkMuted`.
  Normal: `Today` / `Left to target` / `Done`. Quit: `Money saved` / `Urges beaten` / `Best streak`.
- **`ControlBar`** — three labelled controls in one `surface` bar, `radius-lg`, 6dp inner
  padding, each cell ≥60dp:
  - Lock — toggle, `pressed` state, `accentSoft` fill when on, label flips **"Lock" ↔ "Unlock"**
    and the icon flips closed/open.
  - Reverse — toggle, label flips **"Reverse" ↔ "Count up"**.
  - Reset — `danger` text, always opens a sheet first.
- **`MilestoneBadge`** — 56dp round badge plus a label beneath ("1 day", "3 days", …):
  | State | Look |
  | --- | --- |
  | done | `accent` fill, `onAccent` check icon, `ink` label |
  | next | `surface` fill, 2px dashed `accentStrong` ring, the number in `accentStrong`, `ink` label |
  | locked | `surfaceSunken` fill, `inkMuted` number and label |
  Content description reads "{n} day badge, earned" / ", next" / ", locked".
- A `MilestoneRow` that lays the ten badges out in a horizontal scroll, edge-to-edge with
  16dp side padding and the scrollbar hidden.

**Out:** the values themselves and their formatting — TT-022 / TT-023.

## Acceptance criteria

- [ ] `StatStrip` takes exactly three value+label pairs — StatStrip README.
- [ ] Lock and Reverse expose a toggleable state to accessibility services and their
      labels change with state — `NFR-4`, `NFR-6`, ControlBar README.
- [ ] Reset never acts immediately; it raises a confirm callback — `FR-3.6`.
- [ ] The badge row shows earned, next and locked states and names the state in the
      content description, not only by colour — `FR-4.6`, `NFR-6`.
- [ ] Ten badges for 1, 3, 7, 14, 21, 30, 60, 90, 180, 365 — requirements §2.
- [ ] Labels stay readable at 200% font scale (the strip wraps or scales rather than
      clipping) — `NFR-7`.

## Design references

`project/components/StatStrip/README.md`, `ControlBar/README.md`,
`MilestoneBadge/README.md`; `project/Counter.dc.html` (strip + control bar geometry and
the `ctrl(on)` style function); `project/Quit.dc.html` (strip + the `badges` map, which
gives the exact label and aria text).

## Files

- `app/src/main/java/com/taptap/ui/components/StatStrip.kt`
- `app/src/main/java/com/taptap/ui/components/ControlBar.kt`
- `app/src/main/java/com/taptap/ui/components/MilestoneBadge.kt`

## Verification

`@Preview`: both strip variants, the control bar in all four on/off combinations, and the
badge row at 14 days free (so 1/3/7/14 earned, 21 next, the rest locked) in light and dark.
