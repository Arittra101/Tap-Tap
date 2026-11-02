# TT-009 · CounterRepository and the business rules

| | |
| --- | --- |
| Epic | B · Data |
| Type | feature |
| Size | L |
| Depends on | TT-005, TT-006, TT-008 |

## Context

Spec §4.3 is explicit: the counting rules live in the repository, "so Home and the
Counter screen behave the same". Home's `+` button and the Counter screen's dial must
produce the same result, and the only way to guarantee that is to have one
implementation. This is the single most test-worthy file in the app.

## Scope

**In:**
- `observeAll(): Flow<List<Counter>>`, `observeCounter(id): Flow<Counter?>`.
- `tap(id)` — honours `isLocked`; delta is `+step` or `−step` by `isReversed`; clamps at
  0 going down; clamps at `target` going up when `keepCountingAfterTarget` is false;
  writes nothing when the value would not change; logs a `TAP` event with the **actual**
  applied delta.
- `reset(id)` — count goes to `startValue`, logs `RESET`.
- `beatUrge(id)` — logs `URGE_BEATEN` only; never touches `quitAt`.
- `slip(id)` — `bestStreakDays = max(bestStreakDays, daysFree(quitAt, now))`, `quitAt = now`,
  logs `SLIP`. Badges and urge history are untouched.
- `setFocus(id)`, `togglePin(id)`, `toggleLock(id)`, `toggleReverse(id)`.
- `create(counter)` / `update(counter)` / `delete(id)`. `create` clears the old focus
  when the new counter is a focus.
- `observeTodayChange(id)` and `observeUrgesToday(id)` built on `startOfToday()`.
- `private fun CounterEntity.toModel()` at the bottom of this file — no mappers package.

**Out:**
- Dialog state and formatting — the ViewModels.
- Settings — TT-010.

## Acceptance criteria

- [ ] A tap on a locked counter changes nothing and logs nothing — `FR-3.4`.
- [ ] Reversed taps subtract `step` and the count never goes below 0 — `FR-3.5`.
- [ ] With a target and `keepCounting` off, the count stops at the target and further
      taps are no-ops; with it on, counting continues past the target — `FR-3.7`, `FR-1.6`.
- [ ] `reset` goes to `startValue`, not to 0 — `FR-3.6`, ControlBar README.
- [ ] `beatUrge` does not change days free — `FR-4.4`.
- [ ] `slip` keeps the best streak, badges and urges, and restarts days from now — `FR-4.8`.
- [ ] At most one counter is focus after any `setFocus` or focus-creating `create` — `FR-2.6`.
- [ ] Every mutation is a suspend write committed before returning, so a kill right after
      a tap keeps the count — `NFR-3`.
- [ ] A fast burst of taps loses none of them — `NFR-2` (single-writer suspend calls on
      one dispatcher; no read-modify-write race).
- [ ] The repository returns `model` classes; `CounterEntity` never leaves this file —
      spec §4.1.

## Design references

- `project/README.md` → "Behaviour rules" (pin is per group, focus is quit-only and
  single, a quit counter counts days by itself, reset goes to the start value, a new step
  works from the next tap).
- `project/components/Stepper/README.md` → a changed step applies from the next tap and a
  changed start value from the next reset; the current count never jumps.

## Files

- `app/src/main/java/com/taptap/data/repository/CounterRepository.kt`
- `app/src/test/java/com/taptap/data/repository/CounterRepositoryTest.kt`

## Verification

In-memory Room tests, one per acceptance criterion above — this is the list spec §12
asks for: tap, stop at target, reverse never below 0, reset to start value, one focus
only, slip keeps best streak. Plus a burst test: 200 taps in a tight loop end at
`200 × step`.
