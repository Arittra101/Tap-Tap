# TT-030 · Release readiness

| | |
| --- | --- |
| Epic | F · Platform |
| Type | chore |
| Size | M |
| Depends on | TT-028, TT-029, TT-031 |

## Context

The last step of the spec's build order. Three things in the current module would block a
Play release as they stand: `optimization { enable = false }` on the release build type,
no privacy policy to link to, and no size check against `NFR-9`.

## Scope

**In:**
- Turn R8 on for `release` (the template explicitly disables it), add `proguard-rules.pro`
  entries for Room, kotlinx.serialization and Koin, and verify the release build runs.
- Measure the APK/AAB against the 15 MB budget. Backgrounds are drawn, not bundled
  (TT-017), so the font files are the main weight — bundle only the weights actually used.
- Write the privacy policy page and link it from Settings (`FR-6.5`) and the Play listing:
  all data stays on the phone, no account, no analytics with personal data, no ads.
- Play listing: app name **"Tap Tap"**, store title adding "Counter & Quit Habit"
  (TT-031 decision 3).
- Verify Android Auto Backup end to end: back up on one device, restore on another, and
  confirm counters and settings arrive.
- Signing config and a release checklist (version code/name, `targetSdk` at the level
  Play requires).

**Out:** anything in requirements §8's out-of-scope list — accounts, sync, charts,
widgets, Wear OS, iOS, gallery backgrounds, normal-counter reminders.

## Acceptance criteria

- [ ] The release build has R8 enabled and starts, counts, and persists correctly — `NFR-9`.
- [ ] Release artifact is under 15 MB — `NFR-9`.
- [ ] No bitmap background assets ship; backgrounds are vector/drawn — `NFR-9`, TT-017.
- [ ] Auto Backup restores counters and settings onto a second device — `NFR-10`.
- [ ] A privacy policy exists, is linked from Settings and from the Play listing, and
      states that all data stays on the phone with no account, no personal-data analytics
      and no ads — `FR-6.5`, `FR-8.7`.
- [ ] The app name is "Tap Tap" and the store title adds "Counter & Quit Habit" — `TT-031` decision 3.
- [ ] `minSdk 24`, and `targetSdk` at the level Play currently requires — `NFR-8`.
- [ ] The app works with no network at any point — `NFR-1`.

## Design references

`project/README.md` → the app mark (`tt-mark` square in `accent` with the plus glyph) is
the basis for the launcher icon.

## Files

- `app/build.gradle.kts`, `app/proguard-rules.pro`
- `app/src/main/res/mipmap-*/` (launcher icon from the app mark)
- `PRIVACY.md` and the hosted page it is published to
- `app/src/main/res/values*/strings.xml` (app name, privacy link)

## Verification

Build the release AAB, check its size, install it, and run the full flow offline with
airplane mode on. Restore onto a second device and confirm the counters are there.
