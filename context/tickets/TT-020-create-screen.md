# TT-020 · New counter screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | L |
| Depends on | TT-009, TT-015, TT-019 |

## Context

Built before Home (spec build order step 4) so Home has something to show. The type
choice comes first and decides the whole rest of the form (`FR-2.1`), which is why this is
one screen with two field sets rather than two screens.

## Scope

**In:** `CreateContract.kt`, `CreateViewModel.kt`, `CreateScreen.kt` per spec §4.2–4.4.

Layout: `TopBar` (back, "New counter") → the section "What do you want to track?" with two
`TypeCard`s → the type's fields → the create button → the footer line
"You can change the color, background and these options later."

**Normal fields:** name (required, placeholder `e.g. Push-ups, Glasses of water`),
`Start value` with help `Reset goes back here` (min 0, default 0), `Step value` with help
`Added per tap` (min 1, default 1), a `Set a target` switch + `Target count` stepper
(min 1, default 50), a `Keep counting after target` switch with help
`When off, it stops at the target` (default off), `Notes` (optional, placeholder
`When to count, anything to remember`), `Pin to top` with help `Show it first in Counters`.

**Quit fields:** the chip row "Pick a habit or write your own" — the five presets fill the
name:

| Chip | Name it writes |
| --- | --- |
| Cigarettes | `Quit cigarettes` |
| Alcohol | `Quit drinking` |
| Soft drinks | `Quit soft drinks` |
| Phone at night | `Less phone at night` |
| Other | *(empty)* |

then `Habit to quit` (required, placeholder `e.g. Junk food, Late-night snacks`),
`When did you stop?` as a Today / Earlier date segmented control with a date picker when
"Earlier date" is chosen and the help line
`Days free are counted from this day, by themselves. You don't need to tap every day.`;
`Money spent on it per day` (optional, currency prefix, placeholder `150`, help
`We show how much money you save each day.`); a `Set a goal` switch + `Days to stay free`
stepper (default 30); `Your reason to quit` (optional, multi-line, placeholder
`e.g. To breathe better on my morning walk`, help
`We show it to you when you open this counter and when an urge comes.`); and the
**Motivation** group: `Milestone badges` (default on, help `Earn a badge at 1, 3, 7, 14,
30, 90 days and more`), `Daily motivation message` (default on, help `A short note with
your progress every morning`), `Make this my focus` (default off, help `Shown big at the
top of Home. Only one at a time.`), `Pin to top` (default off, help `Show it first in
Quit tracker`).

Create button: `Create counter` for normal, `Start quit counter` for quit. Disabled until
the name has non-blank text. On success the ViewModel emits a `Created` effect and the
Route calls `onCreated`.

**Out:** the counter colour and background (Edit only — `FR-5.2`, `FR-5.3`).

## Acceptance criteria

- [ ] The type is the first field and switching it swaps the form below — `FR-2.1`.
- [ ] Normal fields and their defaults/minimums are exactly as listed — `FR-2.2`.
- [ ] Quit fields, the five habit chips filling the name, and the quit-date choice
      (Today or an earlier date) work as listed — `FR-2.3`.
- [ ] The four motivation options have the stated defaults — `FR-2.4`.
- [ ] Create stays disabled until the name has text — `FR-2.5`.
- [ ] Creating a focus counter removes focus from the previous one — `FR-2.6`.
- [ ] After create the app returns to Home and the counter appears in its group — `FR-2.7`.
- [ ] A quit date later than now is rejected (the picker's max is today).
- [ ] Switching type does not carry a typed name across into the other type's blank form,
      matching the canvas's `pickNormal` / `pickQuit` behaviour.
- [ ] Every string comes from `strings.xml` — `FR-8.1`.
- [ ] The screen scrolls and keeps all labels at 200% font scale — `NFR-7`.

## Design references

`project/Create.dc.html` — artboard "New counter", 390×1560. Its `renderVals` block holds
the habit preset table, the `nameLabel`/`namePlaceholder` switch, `targetLabel`
(`Set a target` / `Set a goal`), `targetUnit` (`Target count` / `Days to stay free`),
`pinHelp` and `createText`. Components: `TypeCard`, `ChipRow`, `TapTapTextField`,
`Stepper`, `SegmentedControl`, `SettingRow`, `Button`.

**Note on the badge help text:** the design says "1, 3, 7, 14, 30, 90 days and more",
which abbreviates the real set. Keep the design's copy — the full ten milestones live in
`TimeUtils.MILESTONES` (TT-006).

## Files

- `app/src/main/java/com/taptap/ui/create/CreateContract.kt`
- `app/src/main/java/com/taptap/ui/create/CreateViewModel.kt`
- `app/src/main/java/com/taptap/ui/create/CreateScreen.kt`

## Verification

Create one normal counter with a target and one quit counter with money, a goal, a reason
and focus on; both appear on Home in the right group. `@Preview` of both form states in
light and dark. A ViewModel test for `canCreate` and for focus-clearing on create.
