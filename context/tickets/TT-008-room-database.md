# TT-008 · Room: entities, DAO, converters, database

| | |
| --- | --- |
| Epic | B · Data |
| Type | feature |
| Size | M |
| Depends on | TT-001, TT-005 |

## Context

`NFR-1` and `NFR-3` require everything stored locally and saved immediately, so a closed
or killed app never loses a count. Spec §7.1–7.2 fixes the schema: **one** `counters`
table holding both counter types with the quit columns nullable, plus a `counter_events`
log that exists now so a stats screen is easy to add later (requirements §8).

## Scope

**In:**
- `CounterEntity` — exactly the columns in spec §7.1, including the nullable QUIT block
  (`quitAt, moneyPerDay, goalDays, reason, bestStreakDays, badgesOn, dailyMessageOn`) and
  a `dailyMessageTime` column for `FR-8.6`'s default 09:00.
- `CounterEventEntity` — `counterId` foreign key with `CASCADE`, indices on `counterId`
  and `at`.
- `Converters.kt` — enums stored as their `name`.
- `AppDatabase` with `exportSchema = true`.
- `CounterDao` — the queries from spec §7.2:
  `observeAll` (ordered `isPinned DESC, sortOrder ASC, createdAt ASC`), `observeById`,
  `getById`, `insert`, `update`, `delete(id)`, `clearFocus`, `setFocusFlag`,
  `@Transaction setFocus`, `insertEvent`, `observeChangeSince`, `observeUrgesSince`.

**Out:**
- Business rules — TT-009. They do **not** belong in the DAO.
- Settings — TT-010 (DataStore, not Room).

## Acceptance criteria

- [ ] Both counter types live in one table; quit-only columns are nullable — spec §7.1.
- [ ] `setFocus` is a single transaction that clears the old focus and sets the new one,
      so two rows can never both be focus — `FR-2.6`, requirements §6 ("at most one true").
- [ ] Deleting a counter cascades its events — no orphan rows.
- [ ] `observeAll` returns pinned rows first — `FR-1.5`, `FR-1.7`.
- [ ] `observeChangeSince` sums only `TAP` deltas; `observeUrgesSince` counts only
      `URGE_BEATEN` rows — `FR-3.2`, `FR-4.4`.
- [ ] The generated schema JSON is committed under `app/schemas/` — spec §7.1.
- [ ] Enums are stored as text, not ordinals, so reordering an enum cannot corrupt data.

## Design references

None — no UI. Field semantics come from requirements §6.

## Files

- `app/src/main/java/com/habitTap/data/db/CounterEntity.kt`
- `app/src/main/java/com/habitTap/data/db/CounterEventEntity.kt`
- `app/src/main/java/com/habitTap/data/db/CounterDao.kt`
- `app/src/main/java/com/habitTap/data/db/Converters.kt`
- `app/src/main/java/com/habitTap/data/db/AppDatabase.kt`
- `app/schemas/com.habitTap.data.db.AppDatabase/1.json` (generated, committed)

## Verification

An in-memory Room test inserting one counter of each type, reading them back, calling
`setFocus` twice and asserting exactly one focus row; deleting a counter and asserting
its events are gone.
