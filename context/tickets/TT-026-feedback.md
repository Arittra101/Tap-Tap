# TT-026 · Feedback: vibration and click

| | |
| --- | --- |
| Epic | F · Platform |
| Type | feature |
| Size | S |
| Depends on | TT-010 |

## Context

`FR-8.4` and `FR-8.5` make every tap confirmable without looking at the screen — which is
the whole point of a one-big-target counter. The two settings from TT-010 switch them.

## Scope

**In:**
- `rememberFeedback()` returning an object with `tap()` and `success()`.
- `tap()` — a short haptic of about 10 ms when vibration is on, plus a soft click when
  sound is on.
- `success()` — a slightly longer haptic, used when a target or a badge is reached.
- Haptics via `HapticFeedback` where it suffices, falling back to `Vibrator` /
  `VibratorManager` with `VibrationEffect` on API 26+ and the deprecated path on 24–25
  (`minSdk` is 24).
- Sound via a `SoundPool` holding one short click, loaded once, released with the
  composition. It plays on the `STREAM_SYSTEM`-equivalent usage so the phone's silent
  mode mutes it.
- Both switches are read live from `SettingsRepository`, so turning one off takes effect
  on the next tap without a restart.

**Out:** calling it — TT-022 handles taps, TT-023 the urge button.

## Acceptance criteria

- [ ] A tap gives about a 10 ms haptic when vibration is on, and nothing when off — `FR-8.4`.
- [ ] Reaching a target or a badge gives a slightly longer haptic — `FR-8.4`.
- [ ] The click respects the phone's silent mode — `FR-8.5`.
- [ ] Sound defaults to off and vibration to on — `FR-6.4`.
- [ ] Feedback never blocks the tap: the count updates whether or not the haptic fires — `NFR-2`.
- [ ] The `SoundPool` is released when the composition leaves — no leak across screens.
- [ ] Works on API 24 and on API 31+ (`VibratorManager`).

## Design references

`project/components/CounterDial/README.md` ("Pair with a vibration of about 10 ms per tap
when vibration is on"); `project/README.md` → "Motion" (reaching a target or badge can use
a short haptic in place of a big animation); `project/Settings.dc.html` → the two switch
rows and their help text.

## Files

- `app/src/main/java/com/habitTap/util/Feedback.kt`
- `app/src/main/res/raw/click.ogg`

## Verification

Manual on a real device (an emulator does not vibrate usefully): tap with vibration on and
off, sound on and off, and with the phone silenced. Tap through a target boundary and feel
the longer haptic.
