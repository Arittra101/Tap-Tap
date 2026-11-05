# TT-023 · Quit counter screen

| | |
| --- | --- |
| Epic | E · Screens |
| Type | feature |
| Size | L |
| Depends on | TT-006, TT-009, TT-014, TT-016, TT-019 |

## Context

Quitting has to feel supported (requirements §1): days free, money saved, badges and the
user's own reason are all on one screen, and the slip path is deliberately gentle.

## Scope

**In:** `QuitContract.kt`, `QuitViewModel.kt`, `QuitScreen.kt`.

Layout: `TopBar` (back, habit name, `Free since {date}` subtitle, pin button) → a 270dp
days ring → the next-milestone line → `StatStrip` → the "I beat an urge" primary button
with its caption → the reason card → the Milestones section → a two-column
"Make focus" / "I slipped" row → the slip `ConfirmSheet`.

- **Ring**: 120dp radius, 16dp stroke, `accent` arc on a `surfaceSunken` track, filled by
  `milestoneProgress`. Inside: a "Your focus" pill when this is the focus, the day count
  at 92sp display, "days free", and `{d} d {h} h without it`.
- Next-milestone line in `accentStrong`: `{n} days to your {m}-day badge`, or
  `You earned every badge` past 365.
- Stat strip: **Money saved** (`daysFree × moneyPerDay`, or `—` when unset),
  **Urges beaten** (total), **Best streak** (`max(bestStreakDays, daysFree)` days).
- "I beat an urge" + caption `{n} beaten today. Tap each time you say no.`
- Reason card: `accentSoft` fill, label "Your reason", the reason in 20sp display, in
  quotes. Hidden when there is no reason.
- Milestones section: heading "Milestones", `{n} of 10 earned`, then the badge row.
- "Make focus" ↔ "Your focus" toggle, and the `dangerOutline` "I slipped".
- A minute ticker so days and hours stay current while the screen is open.

**Out:** badge visuals (TT-014), the sheet shell (TT-016), milestone maths (TT-006).

## Acceptance criteria

- [ ] Top bar shows back, the habit name, "Free since {date}" and a pin button — `FR-4.1`.
- [ ] The ring shows days free and days + hours since the quit date, filled toward the
      next milestone — `FR-4.2`.
- [ ] Stat strip shows money saved, total urges beaten and best streak — `FR-4.3`.
- [ ] "I beat an urge" adds one to today's and the total count and does **not** change
      days free — `FR-4.4`.
- [ ] The reason appears in its own card — `FR-4.5`.
- [ ] The badge row shows earned, next and locked states — `FR-4.6`.
- [ ] "Make focus" / "Your focus" toggles, and making this the focus clears the previous
      one — `FR-4.7`, `FR-2.6`.
- [ ] "I slipped" asks first, then sets the quit date to now, keeping best streak, badges
      and urges — `FR-4.8`.
- [ ] Money uses the local currency symbol — `FR-8.3` (see TT-027).
- [ ] Days and hours refresh at least once a minute — spec §8.
- [ ] With no money set, money saved shows `—` rather than a zero — `FR-1.8` parity.

## Design references

`project/Quit.dc.html` (artboard "Quit counter", 390×1130). Its `renderVals` gives the
exact strings (`sinceText`, `timeText`, `nextText`, the urge caption), the ring geometry
and the slip sheet copy: "That is okay. Your days free start again from today. Your best
streak, badges and urges beaten are kept." → "Start again".

## Files

- `app/src/main/java/com/habitTap/ui/quit/QuitContract.kt`
- `app/src/main/java/com/habitTap/ui/quit/QuitViewModel.kt`
- `app/src/main/java/com/habitTap/ui/quit/QuitScreen.kt`

## Verification

`@Preview` light/dark at 14 days with money and a reason; plus 0 days, 400 days (all
badges earned), no money, no reason. Manual: beat an urge — the total and today's count
rise, days free do not. Slip — days reset to 0, best streak holds at the old value.
