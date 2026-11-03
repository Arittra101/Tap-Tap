# TT-031 · Product decisions

| | |
| --- | --- |
| Epic | G · Decisions |
| Type | spike |
| Size | S |
| Depends on | — |

## Context

Requirements §8 leaves four questions open. Left open they would each block a ticket
mid-build, so they are decided here, with the reasoning kept so nobody has to re-litigate
them later. This ticket produces no code; it binds the tickets listed against each
decision.

## Decision 1 — Start value higher than the target

**Decided:** Edit shows an inline warning under the target field and still allows the
save. This is the requirement's own proposal.

**Why:** it is a legitimate setup (counting *down* to a target from a stock figure, with
reverse on), so blocking it would remove a real use. A warning is enough, because the only
surprising consequence is that a reset lands past the target.

**How to apply:** the caption, in `danger`, reads
`The start value is above the target, so a reset will already be past it.`
Binds **TT-024**.

## Decision 2 — When "Today" resets

**Decided:** at **local midnight**. Not user-configurable in v1.

**Why:** "Today's change" is a glanceable number, not an accounting period. A configurable
day boundary would need its own setting, its own string in six languages and its own
edge cases around DST — all for a stat strip cell. Local midnight is what a user assumes
without being told, and `startOfToday(now, zone)` takes the zone as a parameter so a
chosen boundary can be added later without touching call sites.

**How to apply:** `TimeUtils.startOfToday()` resolves local midnight in the device's
current zone; the repository's "since" queries use it. Binds **TT-006**, **TT-009**, **TT-022**.

## Decision 3 — Final app name

**Decided:** the app is **"HabitTap"**. The Play Store title is
**"HabitTap — Counter & Quit Habit"**.

**Superseded:** this ticket originally recorded the name as "Tap Tap", on the grounds that
it was already the name throughout the design system, every artboard and the brand book,
so renaming would invalidate the app mark for no stated benefit. The product owner has
since chosen **HabitTap**, which also says what the app is for. That reasoning no longer
applies and the rename has been made.

**Why:** "HabitTap" names the job (habits) as well as the gesture (tap), so the launcher
label carries meaning on its own. The store title keeps the searchable words, which is
where they are actually useful.

**How to apply:** `app_name` and `app_wordmark` are both "HabitTap"; the longer form is a
store-listing field, not a string resource. `home_welcome_title` embeds the name and is
translated per locale. Note the applicationId is still `com.taptap` — see the open
question in TT-030. Binds **TT-001**, **TT-030**.

## Decision 4 — The daily message on a day the user slipped

**Decided:** still send it, with wording that acknowledges the restart.

**Why:** the brand book is explicit that a slip is not a failure and that the app says
what is kept. Going silent on the hardest day is the opposite of supportive, and it also
reads as a bug. What must not happen is a message claiming a streak the user no longer
has.

**How to apply:** when `daysFree == 0` because of a slip within the last 24 h, the
notification uses the restart variant — day one framing plus what was kept (best streak,
badges, urges beaten) — rather than the "{n} days free" form. Binds **TT-028**.

## Acceptance criteria

- [ ] Each decision names the tickets it binds, and those tickets reference it back.
- [ ] No ticket in the backlog is blocked on an unresolved question from requirements §8.
- [ ] If a decision is later reversed, the "Why" here is enough to see what it cost.

## Files

This file. No code.

## Verification

`grep -l "TT-031" *.md` lists TT-001, TT-006, TT-009, TT-022, TT-024, TT-028 and TT-030.
