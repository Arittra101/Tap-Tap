# HabitTap — release checklist

## Play Store listing

| Field | Value |
| --- | --- |
| App name (on device) | **HabitTap** |
| Store title | **HabitTap — Counter & Quit Habit** (TT-031 decision 3) |
| Category | Health & Fitness |
| Content rating | Everyone |
| Privacy policy URL | `https://habit-tap-privacy-policy.vercel.app/` — serves `PRIVACY.md`; the app links to the same URL (`SettingsScreen.PRIVACY_POLICY_URL` — **update both together**) |
| Data safety form | "No data collected", "No data shared". Nothing leaves the device (`NFR-1`, `FR-8.7`) |
| Languages | English, Bangla, Hindi, Russian, Spanish, Italian |
| Release notes | `release-notes.txt` — paste the whole file into "What's new" |

## Release notes

`release-notes.txt` holds the "What's new" text for all six languages in the tagged
format the Play Console accepts, so the file can be pasted in whole. Rules it already
follows, worth keeping for later versions:

- **500 characters per language**, counted inside the tags. Play silently truncates
  anything longer.
- **A tag must name a language already enabled on the listing**, or Play rejects the
  entire paste. With English only, paste just the `<en-US>` block.
- Bengali is written as `bn-BD`; if the Play Console dropdown lists it as `bn-IN`,
  change the tag to match.
- Claims in the text are checked against the code — "10 milestone badges, day 1 to
  day 365" is `TimeUtils.MILESTONES`. Re-check them when the features change.
- The five translations have **not** been reviewed by a native speaker, same as the
  in-app strings.

## Signing

The upload key lives in `keystore/`, which is gitignored. Two files:

| File | What it is |
| --- | --- |
| `keystore/habit-tap.jks` | the key itself — **back this up off the machine** |
| `keystore/release.properties` | `keyStore`, `keyStorePassword`, `keyAlias`, `keyAliasPassword` |

`app/build.gradle.kts` reads `release.properties` and signs the release build with it.
When that file is absent — a fresh clone, or CI without the secrets — it falls back to
the `releaseTest` config, which uses the SDK's **debug keystore** so the shrunk build
can still be installed and tested locally. Play rejects anything signed that way, so
`bundleRelease` fails outright rather than producing a bundle that would be refused.

Build the upload artifact with `./gradlew :app:bundleRelease` →
`app/build/outputs/bundle/release/app-release.aab`. Play requires an `.aab`, not an APK.

Enrol in **Play App Signing** at the first upload. The `.jks` then becomes only an
*upload* key, which Google can reset if it is ever lost; decline, and losing it means
the listing can never be updated again. **Never commit a keystore.**

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
      (run before the `com.habitTap` rename; re-run to confirm under the new ID)
- [x] 53 unit tests, 0 failures
- [x] R8 build runs: Room, Navigation 3 keys, Koin and WorkManager all survive minification

## Still to do before upload

- [ ] Replace the `releaseTest` signing config with a real upload key
- [ ] **Have a native speaker review the five translations** — they were produced without one
- [ ] Store screenshots and feature graphic
- [ ] Decide whether `versionCode`/`versionName` move off 1 / "1.0"

## Open after the rename to HabitTap

- `applicationId` and `namespace` are now `com.habitTap`; the Gradle module and the
  checkout directory are still named `TapTap`. That is invisible to users and safe to
  leave. The applicationId itself is permanent once the app is published — the Play
  listing is keyed to it and it can never be changed afterwards — so it must be settled
  before the first upload.
- The design system and the artboards still carry the old "Tap Tap" wordmark. The app no
  longer matches them, so the design source needs the same rename.
