# TT-007 · Koin module, Application and MainActivity

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | S |
| Depends on | TT-001, TT-002, TT-003, TT-004 |

## Context

Spec §6 wants one module file you can read top to bottom to see the whole app, and
constructor injection only. `MainActivity` must extend `AppCompatActivity` for per-app
locales to work below API 33 (spec §7.4).

## Scope

**In:**
- `di/AppModule.kt` — one `module { }` with the database, DAO, `SettingsStore`, both
  repositories and all six ViewModels, exactly in the shape of spec §6. ViewModels that
  need an id are declared as `viewModel { (counterId: Long) -> ... }`.
- `TapTapApp : Application` — `startKoin { androidContext(...); modules(appModule) }`.
  The `DailyMessageWorker` schedule is added here by TT-028.
- `MainActivity : AppCompatActivity` — collects `SettingsRepository.settings` and sets
  content to `TapTapTheme(color, mode) { AppNavigation() }`; `enableEdgeToEdge()`.

**Out:**
- The ViewModel and repository bodies — Epics B and E (this ticket may reference them
  only once they exist; land it after TT-019 if that is simpler).
- Worker scheduling — TT-028. Locale switching — TT-027.

## Acceptance criteria

- [ ] All Koin definitions live in one file — spec §6.
- [ ] No `by inject()` outside `Application` and `Worker` — spec §6.
- [ ] Repositories are concrete classes with no interface — spec §6.
- [ ] `MainActivity` extends `AppCompatActivity` — spec §7.4.
- [ ] The theme reacts to a settings change without restarting the activity — `FR-6.1`, `FR-6.2`.
- [ ] `koin.checkModules()`-style verification passes (or a smoke test resolves every
      definition).

## Design references

None directly; the theme call is TT-002's `TapTapTheme`.

## Files

- `app/src/main/java/com/habitTap/di/AppModule.kt`
- `app/src/main/java/com/habitTap/TapTapApp.kt`
- `app/src/main/java/com/habitTap/MainActivity.kt`

## Verification

Launch the app: it starts without a Koin resolution crash. Change the phone's dark mode
while the app is open with Mode = System and the app follows — `FR-7.2`.
