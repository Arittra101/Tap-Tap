# TT-005 · Model layer

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | S |
| Depends on | TT-001 |

## Context

The spec (§4.1) forbids Room entities crossing into ViewModels, so the UI needs its own
plain Kotlin classes. Requirements §6 defines the fields; spec §7.3 defines the two
computed properties that make Home and the Counter screen agree.

## Scope

**In:**
- `Counter` — the fields from requirements §6 (common + normal + quit), plus:
  - `val isTargetReached get() = target != null && count >= target`
  - `val canTap get() = !isLocked && !(isTargetReached && !keepCountingAfterTarget && !isReversed)`
- `AppSettings` — `themeColor`, `themeMode`, `language`, `vibration`, `sound`.
- `Enums.kt` — `CounterType { NORMAL, QUIT }`, `ThemeColor { PINK, TEAL, SAFFRON, BLUE }`,
  `ThemeMode { LIGHT, DARK, SYSTEM }`, `Background { NONE, WAVES, ORBS, HILLS }`,
  `EventKind { TAP, URGE_BEATEN, RESET, SLIP }`.

**Out:**
- Room entities — TT-008. `toModel()` mapping — TT-009 (a private extension at the
  bottom of the repository file; the spec explicitly forbids a mappers package).

## Acceptance criteria

- [ ] `Counter` holds every field in requirements §6 with the stated types and nullability.
- [ ] `isTargetReached` and `canTap` are computed on the model, so no screen re-derives them.
- [ ] `null` colour means "same as app" — `FR-5.3`, `FR-7.3`.
- [ ] No Android or Compose import in `model/`.

## Design references

Requirements §6 (data model tables). No visual surface.

## Files

- `app/src/main/java/com/habitTap/model/Counter.kt`
- `app/src/main/java/com/habitTap/model/AppSettings.kt`
- `app/src/main/java/com/habitTap/model/Enums.kt`

## Verification

A small JUnit test of `canTap`: locked blocks taps; at target with `keepCounting` off
blocks taps; at target with `keepCounting` on allows them; at target while reversed
allows them.
