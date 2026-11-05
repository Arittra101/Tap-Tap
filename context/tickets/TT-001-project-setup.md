# TT-001 · Gradle, manifest and module config

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | chore |
| Size | M |
| Depends on | — |

## Context

The module is still the Android Studio template: `com.example.taptap`, `minSdk 26`,
AGP `9.3.0-rc01`, and no dependency beyond Compose. The tech spec (§2, §3) names a
different package, a lower `minSdk`, and nine libraries that nothing else can be built
without. This ticket makes the module match the spec so every later ticket compiles.

## Scope

**In:**
- Rename `namespace` and `applicationId` from `com.example.taptap` to `com.habitTap`, and
  move the source tree to `app/src/main/java/com/habitTap/` (tests too).
- `minSdk` 26 → 24.
- Replace AGP `9.3.0-rc01` with **AGP 9.3.0 stable**. The spec forbids RC libraries, and
  9.3.0 was promoted to stable, so the RC is simply out of date.

  **Amended after the fact.** This first went to AGP 9.5.0 — the newest stable — which
  built fine but broke the IDE: Android Studio 2025.2 refuses anything above
  9.3.0-rc01, so the project would not open. 9.3.0 stable satisfies the no-RC rule and
  stays inside what the installed Studio supports. The lesson: "latest stable" has to
  mean latest stable *that the team's tooling accepts*, not latest stable on Maven.
- Add to `gradle/libs.versions.toml` and `app/build.gradle.kts`, all at their latest
  stable versions:
  | Library | Artifacts |
  | --- | --- |
  | Koin | `koin-android`, `koin-androidx-compose` |
  | Room | `room-runtime`, `room-ktx`, `room-compiler` (KSP), `room-testing` |
  | KSP | the `com.google.devtools.ksp` plugin |
  | Navigation 3 | `androidx.navigation3:navigation3-runtime`, `navigation3-ui`, `androidx.lifecycle:lifecycle-viewmodel-navigation3` |
  | DataStore | `datastore-preferences` |
  | AppCompat | `androidx.appcompat:appcompat` |
  | WorkManager | `work-runtime-ktx` |
  | Serialization | `kotlinx-serialization-json` + the `plugin.serialization` plugin |
  | Lifecycle | `lifecycle-runtime-compose` (for `collectAsStateWithLifecycle`) |
  | Fonts | `androidx.compose.ui:ui-text-google-fonts` |
  | Test | `kotlinx-coroutines-test` |
- Room `exportSchema = true` wired to `app/schemas/`, committed to the repo.
- `res/xml/locales_config.xml` listing `en, bn, hi, ru, es, it`, referenced from
  `<application android:localeConfig="...">`.
- `AndroidManifest.xml`: `android:name=".TapTapApp"`, `allowBackup="true"`,
  `fullBackupContent`/`dataExtractionRules` kept, `POST_NOTIFICATIONS` permission.
- Create the empty package folders from the spec's tree (§3) so later tickets have a home.

**Out:**
- Writing `TapTapApp.kt` / `MainActivity.kt` bodies — TT-007.
- Enabling R8 and the size budget — TT-030.
- Translated `strings.xml` files — TT-027 (this ticket only adds `locales_config.xml`).

## Acceptance criteria

- [ ] `./gradlew :app:assembleDebug` succeeds with the new package and dependencies.
- [ ] `minSdk` is 24 and `targetSdk` is 36 — `NFR-8`.
- [ ] No dependency or plugin in the version catalog is alpha, beta or RC, unless no
      stable release exists (note the exception inline if so) — spec §2.
- [ ] Auto Backup is enabled so counters survive a move to a new phone — `NFR-10`.
- [ ] `locales_config.xml` declares exactly the six supported locales — `FR-8.1`.
- [ ] Every package in spec §3 exists under `com.habitTap`.

## Design references

None — this ticket touches no UI.

## Files

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/locales_config.xml` (new)
- `app/src/main/java/com/habitTap/**` (moved from `com/example/taptap`)
- `app/schemas/` (new, committed)

## Verification

`./gradlew :app:assembleDebug` and install on an API 24 emulator — the app launches to
the template screen. `./gradlew :app:dependencies` shows no `-alpha`/`-beta`/`-rc`
coordinates.
