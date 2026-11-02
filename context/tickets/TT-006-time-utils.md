# TT-006 · TimeUtils and milestones

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | M |
| Depends on | TT-001, TT-031 |

## Context

A quit counter counts days by itself from the quit date (`FR-4.2`), the normal counter
shows "today's change" (`FR-3.2`), and badges land on a fixed list of days (`FR-4.6`).
All of that is arithmetic over timestamps, so the spec (§10, §12) puts it in pure
functions that are cheap to test.

## Scope

**In:**
- `val MILESTONES = listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365)`.
- `daysFree(quitAt: Long, now: Long): Int`
- `hoursFree(quitAt: Long, now: Long): Int` — the hours part beyond whole days, for the
  quit ring's "14 d 6 h without it" line.
- `startOfToday(now: Long, zone: ZoneId): Long` — **local midnight** (TT-031 decision 2).
- `nextMilestone(days: Int): Int?` — `null` once 365 is passed.
- `previousMilestone(days: Int): Int` — 0 before the first badge.
- `milestoneProgress(days: Int, hours: Int): Float` — `(days − prev + hours/24) / (next − prev)`,
  clamped 0..1, and `1f` when every badge is earned. This matches the canvas's
  `Quit.dc.html` maths exactly.
- `earnedBadgeCount(days: Int): Int`.

**Out:**
- Formatting the strings ("7 days to your 21-day badge") — that is the screen's job with
  a plural-aware resource, TT-023 / TT-027.

## Acceptance criteria

- [ ] `daysFree` counts whole elapsed days from the quit instant — `FR-4.2`.
- [ ] "Today" starts at local midnight, and a device timezone change is handled by
      passing the zone in rather than caching it — `TT-031` decision 2.
- [ ] `nextMilestone` returns the first milestone strictly greater than `days`, and
      `null` after 365 — `FR-4.6`.
- [ ] `milestoneProgress` is the fraction between the previous and next milestone, not
      progress toward the goal — QuitCard README.
- [ ] Milestone days are exactly 1, 3, 7, 14, 21, 30, 60, 90, 180, 365 — requirements §2.
- [ ] Every function is pure: no `System.currentTimeMillis()` inside, `now` is a parameter.

## Design references

`project/Quit.dc.html` → `TT_MS` and the `frac` / `left` / `nextText` computation;
`project/components/MilestoneBadge/README.md`.

## Files

- `app/src/main/java/com/taptap/util/TimeUtils.kt`
- `app/src/test/java/com/taptap/util/TimeUtilsTest.kt`

## Verification

JUnit tests per spec §12: day boundaries either side of midnight, a DST transition,
`nextMilestone` at 0 / 1 / 14 / 365 / 400, `milestoneProgress` at a milestone exactly
and halfway between two.
