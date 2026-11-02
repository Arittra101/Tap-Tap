# TT-028 · Daily motivation message

| | |
| --- | --- |
| Epic | F · Platform |
| Type | feature |
| Size | M |
| Depends on | TT-006, TT-009, TT-019, TT-027, TT-031 |

## Context

`FR-8.6` is the one piece of background work in v1: one local notification per quit
counter per day, by default at 09:00, carrying days free and the next badge. It is the
only reason WorkManager is in the stack (spec §2).

## Scope

**In:**
- `DailyMessageWorker : CoroutineWorker` — reads every quit counter with
  `dailyMessageOn`, and posts one notification each with its days free and next badge,
  e.g. `14 days free · 7 days to your 21-day badge`.
- Scheduled from `TapTapApp` as a `PeriodicWorkRequest` of 24 h, aligned to each
  counter's `dailyMessageTime` (default 09:00), with `ExistingPeriodicWorkPolicy.UPDATE`
  so a reschedule does not stack duplicates.
- A notification channel created on first run, named for the feature, with the strings
  localised.
- Tapping a notification opens that quit counter's screen (a deep link through
  `QuitKey(counterId)`).
- `POST_NOTIFICATIONS` requested on Android 13+ **at the moment the user turns the option
  on**, not at launch. If the permission is denied, the switch reflects that rather than
  silently doing nothing.
- Per TT-031 decision 4, the message is still sent on a day the user slipped, with wording
  that acknowledges the restart rather than claiming a streak.

**Out:** reminders for normal counters — explicitly out of scope for v1 (requirements §8).

## Acceptance criteria

- [ ] One notification per quit counter per day with `dailyMessageOn`, at 09:00 by
      default — `FR-8.6`.
- [ ] The text carries days free and the next badge — `FR-8.6`.
- [ ] `POST_NOTIFICATIONS` is asked on Android 13+ when the option is switched on — `FR-8.6`.
- [ ] A denied permission is reflected in the UI, not swallowed.
- [ ] Rescheduling does not produce duplicate notifications.
- [ ] Tapping a notification opens that counter.
- [ ] Notification text is localised in all six languages — `FR-8.1`.
- [ ] A counter that slipped today still gets a message, worded for the restart — `TT-031` decision 4.
- [ ] No personal data leaves the device — `FR-8.7`.

## Design references

No artboard covers the notification. Follow the brand book's voice: numbers first, short,
kind, no emoji — `project/README.md` → "Content and voice".

## Files

- `app/src/main/java/com/taptap/util/DailyMessageWorker.kt`
- `app/src/main/java/com/taptap/TapTapApp.kt` (scheduling)
- `app/src/main/res/values*/strings.xml` (notification copy)

## Verification

Trigger the worker directly with `WorkManager`'s test driver and assert one notification
per eligible counter with the right text. Manual: turn the option on with notifications
blocked at the OS level and confirm the UI says so.
