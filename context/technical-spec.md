# Tap Tap — Technical Spec

Version 1 · 2 November 2026 · Owner: Arittra Das
Read with: `REQUIREMENTS.md` (what to build) and the design system `README.md` + `tokens.json` (how it looks).

## 1. Principles

These rules are more important than any single code example in this file.

1. **Simple first.** Write the plain, obvious code. If a junior developer cannot read a file in 5 minutes, make it simpler.
2. **One way to do each thing.** Every screen follows the same MVI pattern. Every screen talks to data the same way.
3. **Flow of calls is always: UI → ViewModel → Repository → DAO / DataStore.** The UI never touches the database. A ViewModel never touches a DAO.
4. **No extra layers.** No use-case classes, no mappers package, no base classes for ViewModels, no generic "Reducer" framework. Add them later only if a real problem needs them.
5. **Small files.** One screen = one folder with 3 files: `XxxContract.kt`, `XxxViewModel.kt`, `XxxScreen.kt`.
6. **No magic.** No reflection, no annotation processors except Room (KSP), no code generation for navigation.

## 2. Tech stack

| Area | Choice | Note |
| --- | --- | --- |
| Language | Kotlin 2.x | |
| UI | Jetpack Compose + Material 3 | Compose BOM |
| Architecture | MVI per screen | State + Intent + Effect |
| DI | Koin (`koin-android`, `koin-androidx-compose`) | No annotations, plain modules |
| Navigation | Navigation 3 (`androidx.navigation3`) | Back stack is a simple list |
| Database | Room (with KSP) | Counters + event log |
| Settings | DataStore Preferences | Theme, mode, language, vibration, sound |
| Async | Kotlin Coroutines + Flow | `StateFlow` for state |
| Serialization | kotlinx.serialization | Only for navigation keys |
| Background | WorkManager | Only for the daily motivation message |
| Languages | AppCompat per-app locales | `AppCompatDelegate.setApplicationLocales` |
| Min / target SDK | 24 / latest required by Google Play | |

Use the **latest stable versions** in `gradle/libs.versions.toml`. Do not use alpha libraries, except where no stable version exists.

## 3. Project structure

One app module. Packages by feature for UI, by type for data.

```
com.taptap
├── TapTapApp.kt                 // Application: starts Koin
├── MainActivity.kt              // sets content: TapTapTheme + AppNavigation
├── di/
│   └── AppModule.kt             // all Koin definitions in one file
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── CounterEntity.kt
│   │   ├── CounterEventEntity.kt
│   │   ├── CounterDao.kt
│   │   └── Converters.kt
│   ├── settings/
│   │   └── SettingsStore.kt     // DataStore read/write
│   └── repository/
│       ├── CounterRepository.kt
│       └── SettingsRepository.kt
├── model/                       // plain Kotlin classes the UI uses
│   ├── Counter.kt
│   ├── AppSettings.kt
│   └── Enums.kt                 // CounterType, ThemeColor, ThemeMode, Background, EventKind
├── navigation/
│   ├── Routes.kt                // all screens as NavKeys
│   └── AppNavigation.kt         // NavDisplay + back stack
├── ui/
│   ├── theme/                   // Color.kt, Type.kt, Shape.kt, Theme.kt, CounterTheme.kt
│   ├── components/              // design system parts: TopBar, CounterDial, FocusCard, ...
│   ├── home/                    // HomeContract.kt, HomeViewModel.kt, HomeScreen.kt
│   ├── create/
│   ├── counter/                 // normal counter screen
│   ├── quit/                    // quit counter screen
│   ├── edit/
│   └── settings/
└── util/
    ├── Feedback.kt              // vibration + click sound
    ├── TimeUtils.kt             // days free, start of today, milestones
    └── DailyMessageWorker.kt
```

## 4. Architecture

### 4.1 Layers

```
Screen (Compose)  ──Intent──▶  ViewModel  ──calls──▶  Repository  ──▶  DAO / DataStore
      ▲                            │                      │
      └────────── State ───────────┘◀────── Flow ─────────┘
      └────────── Effect (one-time) ┘
```

| Layer | Knows about | Does not know about |
| --- | --- | --- |
| Screen | State, Intent, Effect, components | ViewModel internals, Repository, Room |
| ViewModel | Repository, model classes | Compose, Context, Room entities |
| Repository | DAO, DataStore, entities, model classes | ViewModels, Compose |
| DAO | Room entities | Everything else |

### 4.2 MVI contract (one file per screen)

Every screen has exactly these three types:

- **State**: one `data class` with everything the screen draws. Default values make the first frame safe.
- **Intent**: a `sealed interface` of everything the user can do.
- **Effect**: a `sealed interface` of one-time events (go back, open a screen). Use it only for things that must happen once.

```kotlin
// ui/counter/CounterContract.kt
data class CounterState(
    val isLoading: Boolean = true,
    val name: String = "",
    val notes: String = "",
    val count: Long = 0,
    val target: Long? = null,
    val step: Int = 1,
    val todayChange: Long = 0,
    val isLocked: Boolean = false,
    val isReversed: Boolean = false,
    val isTargetReached: Boolean = false,
    val canTap: Boolean = true,
    val showResetDialog: Boolean = false,
    val color: ThemeColor? = null,
    val background: Background = Background.NONE,
)

sealed interface CounterIntent {
    data object Tap : CounterIntent
    data object ToggleLock : CounterIntent
    data object ToggleReverse : CounterIntent
    data object ResetClicked : CounterIntent
    data object ResetConfirmed : CounterIntent
    data object ResetDismissed : CounterIntent
}

sealed interface CounterEffect {
    data object TapFeedback : CounterEffect   // vibrate + click
}
```

### 4.3 ViewModel pattern

Every ViewModel looks like this. Copy it for each new screen.

```kotlin
class CounterViewModel(
    private val counterId: Long,
    private val repository: CounterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()

    private val _effects = Channel<CounterEffect>(Channel.BUFFERED)
    val effects: Flow<CounterEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeCounter(counterId).collect { counter ->
                if (counter == null) return@collect
                _state.update { it.copy(
                    isLoading = false,
                    name = counter.name,
                    count = counter.count,
                    target = counter.target,
                    step = counter.step,
                    isLocked = counter.isLocked,
                    isReversed = counter.isReversed,
                    isTargetReached = counter.isTargetReached,
                    canTap = counter.canTap,
                ) }
            }
        }
    }

    fun onIntent(intent: CounterIntent) {
        when (intent) {
            CounterIntent.Tap -> tap()
            CounterIntent.ToggleLock -> viewModelScope.launch { repository.toggleLock(counterId) }
            CounterIntent.ToggleReverse -> viewModelScope.launch { repository.toggleReverse(counterId) }
            CounterIntent.ResetClicked -> _state.update { it.copy(showResetDialog = true) }
            CounterIntent.ResetDismissed -> _state.update { it.copy(showResetDialog = false) }
            CounterIntent.ResetConfirmed -> {
                _state.update { it.copy(showResetDialog = false) }
                viewModelScope.launch { repository.reset(counterId) }
            }
        }
    }

    private fun tap() {
        if (!_state.value.canTap) return
        viewModelScope.launch {
            repository.tap(counterId)
            _effects.send(CounterEffect.TapFeedback)
        }
    }
}
```

Rules:

- Public API of a ViewModel = `state`, `effects` and `onIntent()`. Nothing else.
- Use `_state.update { it.copy(...) }` for every change.
- Business rules (stop at target, never below 0, reset to start value) live in the **Repository**, so Home and the Counter screen behave the same.
- Screen-only flags (a dialog is open) live only in the ViewModel state.

### 4.4 Screen pattern

Split every screen into two composables:

- `XxxRoute`: gets the ViewModel, collects state and effects. Used by navigation.
- `XxxScreen`: pure UI. Takes `state` and `onIntent`. Easy to preview.

```kotlin
@Composable
fun CounterRoute(
    viewModel: CounterViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberFeedback()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CounterEffect.TapFeedback -> feedback.tap()
            }
        }
    }

    CounterScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack, onEdit = onEdit)
}

@Composable
fun CounterScreen(
    state: CounterState,
    onIntent: (CounterIntent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) { /* layout from the design: TopBar, StatStrip, CounterDial, ControlBar */ }
```

**Navigation stays outside the ViewModel.** Simple moves (back, open a screen) are plain lambdas from the screen. If a move must happen after saving (Create, Edit), the ViewModel sends an Effect (`Saved`) and the Route calls the lambda.

## 5. Navigation (Navigation 3)

Navigation 3 treats the back stack as a list. Opening a screen = add a key to the list. Going back = remove the last key. That is all.

### 5.1 Routes

All screens in one file. A key holds only the arguments the screen needs (usually an id).

```kotlin
// navigation/Routes.kt
@Serializable data object HomeRoute : NavKey
@Serializable data object CreateRoute : NavKey
@Serializable data class CounterRoute(val counterId: Long) : NavKey
@Serializable data class QuitRoute(val counterId: Long) : NavKey
@Serializable data class EditRoute(val counterId: Long) : NavKey
@Serializable data object SettingsRoute : NavKey
```

### 5.2 The navigation host

One file. Each `entry` reads like a sentence: "for this route, show this screen; when the user does X, go to Y".

```kotlin
// navigation/AppNavigation.kt
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(HomeRoute)

    fun open(route: NavKey) = backStack.add(route)
    fun goBack() = backStack.removeLastOrNull()

    NavDisplay(
        backStack = backStack,
        onBack = { goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),   // each screen gets its own ViewModel
        ),
        entryProvider = entryProvider {

            entry<HomeRoute> {
                HomeRoute(
                    viewModel = koinViewModel(),
                    onOpenCounter = { id -> open(CounterRoute(id)) },
                    onOpenQuit = { id -> open(QuitRoute(id)) },
                    onCreate = { open(CreateRoute) },
                    onSettings = { open(SettingsRoute) },
                )
            }

            entry<CreateRoute> {
                CreateRoute(
                    viewModel = koinViewModel(),
                    onBack = { goBack() },
                    onCreated = { goBack() },
                )
            }

            entry<CounterRoute> { route ->
                CounterRoute(
                    viewModel = koinViewModel { parametersOf(route.counterId) },
                    onBack = { goBack() },
                    onEdit = { open(EditRoute(route.counterId)) },
                )
            }

            entry<QuitRoute> { route ->
                QuitRoute(
                    viewModel = koinViewModel { parametersOf(route.counterId) },
                    onBack = { goBack() },
                    onEdit = { open(EditRoute(route.counterId)) },
                )
            }

            entry<EditRoute> { route ->
                EditRoute(
                    viewModel = koinViewModel { parametersOf(route.counterId) },
                    onBack = { goBack() },
                    onSaved = { goBack() },
                    onDeleted = {
                        backStack.clear()          // the counter is gone: go to Home
                        backStack.add(HomeRoute)
                    },
                )
            }

            entry<SettingsRoute> {
                SettingsRoute(viewModel = koinViewModel(), onBack = { goBack() })
            }
        },
    )
}
```

Navigation rules:

- Only `AppNavigation.kt` touches `backStack`. Screens get lambdas like `onBack` and `onOpenCounter`.
- Pass ids, never whole objects. The next screen loads its own data.
- Name lambdas by what the user did (`onCreated`, `onDeleted`), not by where they go.
- Name clash note: the `Route` composables (`HomeRoute(...)`) and the keys (`HomeRoute`) can share a name in different packages. If that confuses the team, name the composables `HomeEntry` instead. Pick one style and keep it.

## 6. Dependency injection (Koin)

One module file. Read it top to bottom to see the whole app.

```kotlin
// di/AppModule.kt
val appModule = module {
    // Data
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "taptap.db").build() }
    single { get<AppDatabase>().counterDao() }
    single { SettingsStore(androidContext()) }

    // Repositories
    single { CounterRepository(get()) }
    single { SettingsRepository(get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CreateViewModel(get()) }
    viewModel { (counterId: Long) -> CounterViewModel(counterId, get()) }
    viewModel { (counterId: Long) -> QuitViewModel(counterId, get()) }
    viewModel { (counterId: Long) -> EditViewModel(counterId, get()) }
    viewModel { SettingsViewModel(get()) }
}

// TapTapApp.kt
class TapTapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TapTapApp)
            modules(appModule)
        }
    }
}
```

Rules: constructor injection only. No `by inject()` inside classes except `Application` and `Worker`. Repositories are concrete classes (no interface) until a second implementation is needed.

## 7. Data layer

### 7.1 Database: two tables

Keep both counter types in **one table**. Quit-only fields are nullable. This keeps queries and code simple.

```kotlin
@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: CounterType,                 // NORMAL, QUIT
    val name: String,
    val notes: String? = null,
    val color: ThemeColor? = null,         // null = same as app
    val background: Background = Background.NONE,
    val isPinned: Boolean = false,
    val isFocus: Boolean = false,          // QUIT only, max one true
    val sortOrder: Int = 0,
    val createdAt: Long,                   // epoch millis

    // NORMAL
    val count: Long = 0,
    val startValue: Long = 0,
    val stepValue: Int = 1,
    val target: Long? = null,
    val keepCountingAfterTarget: Boolean = false,
    val isLocked: Boolean = false,
    val isReversed: Boolean = false,

    // QUIT
    val quitAt: Long? = null,              // epoch millis
    val moneyPerDay: Double? = null,
    val goalDays: Int? = null,
    val reason: String? = null,
    val bestStreakDays: Int = 0,
    val badgesOn: Boolean = true,
    val dailyMessageOn: Boolean = true,
)

@Entity(
    tableName = "counter_events",
    foreignKeys = [ForeignKey(CounterEntity::class, ["id"], ["counterId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("counterId"), Index("at")],
)
data class CounterEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,
    val kind: EventKind,                   // TAP, URGE_BEATEN, RESET, SLIP
    val delta: Long = 0,                   // +step or -step for TAP
    val at: Long,                          // epoch millis
)
```

Store enums as their `name` with a simple `TypeConverter`. Turn on `exportSchema = true` and keep schema files in Git, so later migrations are easy.

### 7.2 DAO

Plain queries. Lists come back as `Flow`, so the UI updates by itself.

```kotlin
@Dao
interface CounterDao {
    @Query("SELECT * FROM counters ORDER BY isPinned DESC, sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE id = :id")
    fun observeById(id: Long): Flow<CounterEntity?>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getById(id: Long): CounterEntity?

    @Insert suspend fun insert(counter: CounterEntity): Long
    @Update suspend fun update(counter: CounterEntity)
    @Query("DELETE FROM counters WHERE id = :id") suspend fun delete(id: Long)

    @Query("UPDATE counters SET isFocus = 0 WHERE isFocus = 1")
    suspend fun clearFocus()

    @Transaction
    suspend fun setFocus(id: Long) { clearFocus(); setFocusFlag(id) }

    @Query("UPDATE counters SET isFocus = 1 WHERE id = :id")
    suspend fun setFocusFlag(id: Long)

    @Insert suspend fun insertEvent(event: CounterEventEntity)

    @Query("SELECT COALESCE(SUM(delta), 0) FROM counter_events WHERE counterId = :id AND kind = 'TAP' AND at >= :since")
    fun observeChangeSince(id: Long, since: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM counter_events WHERE counterId = :id AND kind = 'URGE_BEATEN' AND at >= :since")
    fun observeUrgesSince(id: Long, since: Long): Flow<Int>
}
```

### 7.3 Repository

The repository holds **all business rules**. It returns `model` classes, not entities.

```kotlin
class CounterRepository(private val dao: CounterDao) {

    fun observeAll(): Flow<List<Counter>> = dao.observeAll().map { list -> list.map { it.toModel() } }
    fun observeCounter(id: Long): Flow<Counter?> = dao.observeById(id).map { it?.toModel() }

    suspend fun tap(id: Long) {
        val c = dao.getById(id) ?: return
        if (c.isLocked) return
        val delta = if (c.isReversed) -c.stepValue.toLong() else c.stepValue.toLong()
        var next = (c.count + delta).coerceAtLeast(0)
        if (!c.isReversed && c.target != null && !c.keepCountingAfterTarget) next = next.coerceAtMost(c.target)
        if (next == c.count) return
        dao.update(c.copy(count = next))
        dao.insertEvent(CounterEventEntity(counterId = id, kind = EventKind.TAP, delta = next - c.count, at = now()))
    }

    suspend fun reset(id: Long) {
        val c = dao.getById(id) ?: return
        dao.update(c.copy(count = c.startValue))
        dao.insertEvent(CounterEventEntity(counterId = id, kind = EventKind.RESET, at = now()))
    }

    suspend fun beatUrge(id: Long) =
        dao.insertEvent(CounterEventEntity(counterId = id, kind = EventKind.URGE_BEATEN, at = now()))

    suspend fun slip(id: Long) {
        val c = dao.getById(id) ?: return
        val days = TimeUtils.daysFree(c.quitAt ?: now(), now())
        dao.update(c.copy(quitAt = now(), bestStreakDays = maxOf(c.bestStreakDays, days)))
        dao.insertEvent(CounterEventEntity(counterId = id, kind = EventKind.SLIP, at = now()))
    }

    suspend fun setFocus(id: Long) = dao.setFocus(id)
    suspend fun togglePin(id: Long) { val c = dao.getById(id) ?: return; dao.update(c.copy(isPinned = !c.isPinned)) }
    suspend fun toggleLock(id: Long) { val c = dao.getById(id) ?: return; dao.update(c.copy(isLocked = !c.isLocked)) }
    suspend fun toggleReverse(id: Long) { val c = dao.getById(id) ?: return; dao.update(c.copy(isReversed = !c.isReversed)) }

    private fun now() = System.currentTimeMillis()
}
```

Put `toModel()` as a private extension at the bottom of the repository file. No separate mapper classes.

The model keeps simple "computed" answers next to the data, so every screen gets the same answer:

```kotlin
// model/Counter.kt
data class Counter(
    val id: Long, val type: CounterType, val name: String, val notes: String?,
    val color: ThemeColor?, val background: Background, val isPinned: Boolean, val isFocus: Boolean,
    val count: Long, val startValue: Long, val step: Int, val target: Long?,
    val keepCountingAfterTarget: Boolean, val isLocked: Boolean, val isReversed: Boolean,
    val quitAt: Long?, val moneyPerDay: Double?, val goalDays: Int?, val reason: String?, val bestStreakDays: Int,
) {
    val isTargetReached: Boolean get() = target != null && count >= target
    val canTap: Boolean get() = !isLocked && !(isTargetReached && !keepCountingAfterTarget && !isReversed)
}
```

### 7.4 Settings (DataStore)

`SettingsStore` reads and writes 5 keys. `SettingsRepository` exposes `val settings: Flow<AppSettings>` and one `suspend fun` per setting.

| Key | Type | Default |
| --- | --- | --- |
| theme_color | String (ThemeColor) | PINK |
| theme_mode | String (ThemeMode) | SYSTEM |
| language | String (tag) | empty = phone language |
| vibration | Boolean | true |
| sound | Boolean | false |

Language: on change, call `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))`. Declare the 6 locales in `res/xml/locales_config.xml`. `MainActivity` extends `AppCompatActivity` for this to work on old Android versions.

## 8. Screens: state and intents

| Screen | State (main fields) | Intents |
| --- | --- | --- |
| Home | focus: Counter?, normalCounters, quitCounters, isEmpty | AddTap(id), BeatUrge(id), TogglePin(id), MakeFocus(id) |
| Create | type, name, habitPreset, startValue, step, targetOn, target, keepCounting, notes, quitToday, quitDate, moneyPerDay, goalOn, goalDays, reason, badgesOn, dailyOn, makeFocus, pinned, canCreate | one intent per field change + Create |
| Counter | see 4.2 | Tap, ToggleLock, ToggleReverse, Reset… |
| Quit | name, quitAt, daysFree, hoursFree, nextMilestone, milestoneProgress, moneySaved, urgesToday, urgesTotal, bestStreak, reason, isFocus, isPinned, showSlipDialog | BeatUrge, ToggleFocus, TogglePin, SlipClicked, SlipConfirmed, SlipDismissed |
| Edit | same fields as Create for its type + color, background | one intent per field + Save, Delete, DeleteConfirmed |
| Settings | color, mode, language, vibration, sound | ChangeColor, ChangeMode, ChangeLanguage, ToggleVibration, ToggleSound |

Home builds its lists in the ViewModel with `combine(repository.observeAll(), ...)`:

- `focus` = the quit counter with `isFocus == true`.
- `normalCounters` = type NORMAL (already pinned-first from the DAO).
- `quitCounters` = type QUIT without the focus.

Days free must refresh while a quit screen is open. Use a simple ticker in the Quit and Home ViewModels:

```kotlin
private val minuteTicker = flow { while (true) { emit(Unit); delay(60_000) } }
```

## 9. Theme

Colors come from the design system `tokens.json`. Copy the values into Kotlin once.

```kotlin
data class TapTapColors(
    val bg: Color, val surface: Color, val surfaceRaised: Color, val surfaceSunken: Color,
    val ink: Color, val inkMuted: Color, val line: Color, val lineStrong: Color,
    val accent: Color, val onAccent: Color, val accentStrong: Color, val accentSoft: Color, val onAccentSoft: Color,
    val danger: Color, val onDanger: Color, val success: Color, val onSuccess: Color, val scrim: Color,
)

val LocalTapTapColors = staticCompositionLocalOf { lightPink }

fun tapTapColors(color: ThemeColor, dark: Boolean): TapTapColors = ... // 8 combinations

@Composable
fun TapTapTheme(color: ThemeColor, mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = tapTapColors(color, dark)
    CompositionLocalProvider(LocalTapTapColors provides colors, LocalIsDark provides dark) {
        MaterialTheme(colorScheme = colors.toMaterialScheme(dark), typography = TapTapTypography, content = content)
    }
}

/** Per-counter color: changes accent colors only, keeps light/dark. */
@Composable
fun CounterTheme(color: ThemeColor?, content: @Composable () -> Unit) {
    if (color == null) return content()
    val colors = tapTapColors(color, LocalIsDark.current)
    CompositionLocalProvider(LocalTapTapColors provides colors, content = content)
}
```

- Access colors with a short helper: `val c = TapTapTheme.colors` (an object with a `@Composable get()`).
- Fonts: Outfit and Noto Sans as downloadable Google Fonts (`ui-text-google-fonts`), or bundled in `res/font` if offline first launch matters. Use `fontFeatureSettings = "tnum"` for numbers.
- Components in `ui/components` match the design system names: `TopBar`, `CounterDial`, `FocusCard`, `FocusPicker`, `CounterCard`, `QuitCard`, `StatStrip`, `ControlBar`, `MilestoneBadge`, `TypeCard`, `ChipRow`, `TapTapTextField`, `Stepper`, `SettingRow`, `SegmentedControl`, `ColorSwatch`, `ConfirmSheet`, `StatusPill`.

## 10. Utilities

| File | What it does |
| --- | --- |
| `TimeUtils.kt` | `daysFree(quitAt, now)`, `hoursFree(...)`, `startOfToday()`, `nextMilestone(days)`, `milestoneProgress(days)`. Pure functions, easy to test. |
| `Feedback.kt` | `rememberFeedback()` returns an object with `tap()` and `success()`. Reads the vibration and sound settings. Uses `HapticFeedback` / `Vibrator` and a `SoundPool` with one short click sound. |
| `DailyMessageWorker.kt` | A `CoroutineWorker` that runs once a day and posts one notification per quit counter with `dailyMessageOn`. Scheduled with `PeriodicWorkRequest` (24 h) from `TapTapApp`. Ask for `POST_NOTIFICATIONS` on Android 13+ when the user turns this option on. |

Milestones: `val MILESTONES = listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365)`.

## 11. Coding rules

- Name things by what they are: `CounterRepository.tap()`, not `CounterRepository.handleIncrementAction()`.
- One public composable per screen file (`XxxRoute`), plus the `XxxScreen` and small private parts.
- Every `XxxScreen` has a `@Preview` in light and dark.
- No hard-coded text. All strings in `strings.xml` for en, bn, hi, ru, es, it.
- No hard-coded colors or sizes in screens. Use `TapTapTheme.colors` and the spacing values (`4, 8, 12, 16, 24, 32, 48, 64.dp`).
- Use `collectAsStateWithLifecycle()` for state.
- Keep functions under about 40 lines. Split UI into small private composables with clear names.
- Write a short KDoc only where the "why" is not clear from the code.

## 12. Testing

Keep tests few and useful.

| What | How |
| --- | --- |
| `TimeUtils` | Plain JUnit tests: days free, milestones, start of today. |
| `CounterRepository` rules | In-memory Room database: tap, stop at target, reverse never below 0, reset to start value, one focus only, slip keeps best streak. |
| ViewModels | Test 2–3 important intents each with an in-memory Room repository and `kotlinx-coroutines-test`. |
| UI | Compose previews for every screen; a few UI tests later if needed. |

## 13. Build order

Each step ends with a running app.

1. Project setup: version catalog, Koin, Room, Navigation 3, theme (8 color sets, fonts).
2. Database, DAO, `CounterRepository` + tests for the rules.
3. Navigation with empty screens and working back.
4. Create screen (Normal and Quit).
5. Home (Focus card, counters row, quit list, empty state, no-focus state).
6. Normal counter screen.
7. Quit counter screen.
8. Edit screen.
9. Settings (theme, mode, language, vibration, sound, privacy link).
10. Daily message worker, final polish, Play Store build.