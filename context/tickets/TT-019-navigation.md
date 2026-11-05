# TT-019 · Navigation 3 host and routes

| | |
| --- | --- |
| Epic | D · Navigation |
| Type | feature |
| Size | M |
| Depends on | TT-001, TT-007 |

## Context

Spec §5 picks Navigation 3 and keeps it deliberately plain: the back stack is a list,
opening a screen adds a key, going back removes the last one. Spec build order step 3
lands this with empty screens so back navigation is testable before any screen exists.

## Scope

**In:**
- `navigation/Routes.kt` — six `@Serializable` NavKeys:
  `HomeKey`, `CreateKey`, `CounterKey(counterId: Long)`, `QuitKey(counterId: Long)`,
  `EditKey(counterId: Long)`, `SettingsKey`.
- `navigation/AppNavigation.kt` — `rememberNavBackStack(HomeKey)`, local `open()` /
  `goBack()`, `NavDisplay` with `rememberSaveableStateHolderNavEntryDecorator()` and
  `rememberViewModelStoreNavEntryDecorator()` so each screen gets its own ViewModel, and
  one `entry<…>` per route wiring `koinViewModel()` / `koinViewModel { parametersOf(id) }`.
- Edit's `onDeleted` clears the stack and pushes `HomeKey`, because the counter it came
  from no longer exists.
- Placeholder composables for all six screens so this ticket ships running and each
  Epic E ticket only swaps one of them out.
- The naming decision the spec leaves open (§5.2 "Name clash note"): the keys are
  `XxxKey` and the composables are `XxxRoute`/`XxxScreen`. Pick this and keep it.

**Out:** the screens themselves — Epic E.

## Acceptance criteria

- [ ] Only `AppNavigation.kt` touches the back stack; screens receive lambdas — spec §5.
- [ ] Keys carry ids only, never whole objects; the target screen loads its own data — spec §5.
- [ ] Lambdas are named for what the user did (`onCreated`, `onDeleted`, `onSaved`),
      not for where they go — spec §5.
- [ ] Each screen keeps its own ViewModel across recomposition and survives rotation.
- [ ] Deleting a counter returns to Home with no dead entry left behind — `FR-5.7`.
- [ ] System back works from every screen.
- [ ] No navigation logic sits inside a ViewModel; a post-save move goes through an
      `Effect` the Route handles — spec §4.4.

## Design references

The artboards' `<a href>` targets encode the intended graph: Home → Settings, Home →
Create, Home → Counter/Quit, Counter → Edit, Quit → Edit, and every inner screen → Home.

## Files

- `app/src/main/java/com/habitTap/navigation/Routes.kt`
- `app/src/main/java/com/habitTap/navigation/AppNavigation.kt`

## Verification

Run the app and walk the whole graph with the placeholders, using system back from each
screen. Rotate on a counter screen and confirm the key and its ViewModel survive.
