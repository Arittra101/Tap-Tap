# HabitTap — release checklist

## Play Store listing

| Field | Value |
| --- | --- |
| App name (on device) | **HabitTap** |
| Store title | **HabitTap — Counter & Quit Habit** (TT-031 decision 3) |
| Category | Health & Fitness |
| Content rating | Everyone |
| Privacy policy URL | must serve `PRIVACY.md`; the app links to `https://taptap.app/privacy` (`SettingsScreen.PRIVACY_POLICY_URL` — **update both together**) |
| Data safety form | "No data collected", "No data shared". Nothing leaves the device (`NFR-1`, `FR-8.7`) |
| Languages | English, Bangla, Hindi, Russian, Spanish, Italian |

## Signing

`app/build.gradle.kts` has a `releaseTest` signing config pointing at the **debug
keystore**. It exists so the shrunk build can be installed and tested; it is *not* an
upload key.

Before the first upload: enrol in Play App Signing and replace `releaseTest` with a real
upload key supplied from outside the repo (`~/.gradle/gradle.properties` or CI secrets).
**Never commit a keystore.**

## Size (NFR-9: under 15 MB)

| Artifact | Size |
| --- | --- |
| Release APK (universal) | **3.6 MB** |
| Release AAB | 6.2 MB (Play delivers a per-device slice, smaller still) |

Breakdown of the APK:

| Part | Size |
| --- | --- |
| `classes.dex` + `classes2.dex` | 3.2 MB |
| `res` | 2.3 MB — of which **Noto Sans is 2.0 MB** |
| everything else | < 0.6 MB |

Backgrounds add nothing: Waves, Orbs and Hills are drawn on a Canvas, not shipped as
images (TT-017).

**If the budget ever gets tight,** the first lever is Noto Sans: it is 55% of the APK,
and the bundled file is the full variable font with every script and width axis.
Subsetting it to Latin + Cyrillic would take it to roughly 300 KB. It is bundled rather
than downloaded so a first launch with no network still shows the real typefaces
(`NFR-1`) — verified in airplane mode.

## Verified before release

- [x] `NFR-1` Works fully offline — airplane mode: launch, create a counter, count to 7
- [x] `NFR-2` A burst of taps loses none — 200 concurrent taps in test, 25 on device
- [x] `NFR-3` A tap survives a force-stop
- [x] `NFR-4` Every icon-only control has a label; targets ≥48dp
- [x] `NFR-5` 160 colour pairings across all 8 themes meet 4.5:1 / 3:1 — computed
- [x] `NFR-6` Target reached, locked and slips each carry a word or icon
- [x] `NFR-7` 200% font scale on a 360dp phone, all screens, nothing clipped
- [x] `NFR-8` minSdk 24, targetSdk 36
- [x] `NFR-9` 3.6 MB release APK, R8 + resource shrinking on
- [x] `NFR-10` Auto Backup round trip — `restoreFinished packageName=com.taptap`
- [x] 53 unit tests, 0 failures
- [x] R8 build runs: Room, Navigation 3 keys, Koin and WorkManager all survive minification

## Still to do before upload

- [ ] Host the privacy policy and point `PRIVACY_POLICY_URL` at the real address
- [ ] Replace the `releaseTest` signing config with a real upload key
- [ ] **Have a native speaker review the five translations** — they were produced without one
- [ ] Store screenshots and feature graphic
- [ ] Decide whether `versionCode`/`versionName` move off 1 / "1.0"

## Open after the rename to HabitTap

- `applicationId` is still `com.taptap`, and the Gradle module/dir are still `TapTap`.
  This is invisible to users, but it is permanent once the app is published: the Play
  listing is keyed to it and it can never be changed afterwards. Decide before the first
  upload whether it should become `com.habittap`.
- The design system and the artboards still carry the old "Tap Tap" wordmark. The app no
  longer matches them, so the design source needs the same rename.
