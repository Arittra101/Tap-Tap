# Tap Tap — ticket board

> **Status: all 31 tickets complete.** The app builds, runs and is verified on device.
> See `../../RELEASE.md` for what remains before a Play upload (a real upload key, a
> hosted privacy policy, and a native review of the five translations).

31 tickets covering v1, derived from `../requirment.md`, `../technical-spec.md` and the
two design artifacts. Order follows the tech spec's build order (§13): **every epic ends
with a running app.**

## Sources

| Source | How to read it |
| --- | --- |
| `../requirment.md` | 6 screens, FR-1.1 … FR-8.7, NFR-1 … NFR-10, the data model |
| `../technical-spec.md` | MVI per screen, Koin, Room+KSP, Navigation 3, DataStore, the package tree, code patterns |
| `../design/design-system` → `https://claude.ai/artifact/GNZmU3jbmFrVRjh9nKAnqn` | `project/README.md` (brand book), `project/tokens.json`, 20 component READMEs |
| `../design/design-url` → `https://claude.ai/artifact/99CsBi4P6M6cYeuDuyoCAx` | 11 artboards: `project/Main.dc.html`, `Create`, `Counter`, `Quit`, `Edit`, `Settings` + 5 theme/state variants |

Both design links are Claude artifacts: read them with the **Artifact** tool's `read`
action and a file `path` (not WebFetch, which only sees the editor shell). The variant
artboards (`HomeDark`, `HomeEmpty`, `HomeNoFocus`, `CounterDark`, `SettingsDark`) just
re-import the six main ones with `color` / `mode` / `variant` / `bg` props — the content
lives in the six.

## Epics

### A · Foundation — the app compiles and is themed
| | | |
| --- | --- | --- |
| [TT-001](TT-001-project-setup.md) | Gradle, manifest and module config | M |
| [TT-002](TT-002-theme-tokens.md) | Colour tokens and the 8 app themes | L |
| [TT-003](TT-003-typography.md) | Typography and fonts | M |
| [TT-004](TT-004-shape-spacing.md) | Shape and spacing tokens | S |
| [TT-005](TT-005-model-layer.md) | Model layer | S |
| [TT-006](TT-006-time-utils.md) | TimeUtils and milestones | M |
| [TT-007](TT-007-koin-app-activity.md) | Koin module, Application and MainActivity | S |

### B · Data — the rules exist and are tested
| | | |
| --- | --- | --- |
| [TT-008](TT-008-room-database.md) | Room: entities, DAO, converters, database | M |
| [TT-009](TT-009-counter-repository.md) | CounterRepository and the business rules | L |
| [TT-010](TT-010-settings-store.md) | SettingsStore and SettingsRepository | S |

### C · Components — the design system in Compose
| | | |
| --- | --- | --- |
| [TT-011](TT-011-bars-and-buttons.md) | TopBar, IconButton, Button, StatusPill | M |
| [TT-012](TT-012-counter-dial.md) | CounterDial | L |
| [TT-013](TT-013-home-cards.md) | CounterCard, QuitCard, FocusCard, FocusPicker | L |
| [TT-014](TT-014-stat-strip-control-bar-badges.md) | StatStrip, ControlBar, MilestoneBadge | M |
| [TT-015](TT-015-form-components.md) | Form components | L |
| [TT-016](TT-016-confirm-sheet.md) | ConfirmSheet | S |
| [TT-017](TT-017-counter-backgrounds.md) | Counter backgrounds (Waves, Orbs, Hills) | M |
| [TT-018](TT-018-icon-set.md) | Icon set | S |

### D · Navigation
| | | |
| --- | --- | --- |
| [TT-019](TT-019-navigation.md) | Navigation 3 host and routes | M |

### E · Screens
| | | |
| --- | --- | --- |
| [TT-020](TT-020-create-screen.md) | New counter screen | L |
| [TT-021](TT-021-home-screen.md) | Home screen | L |
| [TT-022](TT-022-normal-counter-screen.md) | Normal counter screen | M |
| [TT-023](TT-023-quit-counter-screen.md) | Quit counter screen | L |
| [TT-024](TT-024-edit-screen.md) | Edit counter screen | L |
| [TT-025](TT-025-settings-screen.md) | Settings screen | M |

### F · Platform and polish
| | | |
| --- | --- | --- |
| [TT-026](TT-026-feedback.md) | Feedback: vibration and click | S |
| [TT-027](TT-027-localization.md) | Localisation and number formatting | L |
| [TT-028](TT-028-daily-message-worker.md) | Daily motivation message | M |
| [TT-029](TT-029-accessibility-pass.md) | Accessibility and font-scaling pass | M |
| [TT-030](TT-030-release-readiness.md) | Release readiness | M |

### G · Decisions
| | | |
| --- | --- | --- |
| [TT-031](TT-031-product-decisions.md) | Product decisions (requirements §8) | S |

## What constrains the order

Most of the backlog is parallelisable. The edges that actually matter:

```
TT-001 ─┬─> TT-002 ─┬─> all of Epic C
        ├─> TT-003 ─┤
        ├─> TT-004 ─┘
        ├─> TT-005 ──> TT-008 ──> TT-009 ──┐
        ├─> TT-006 ────────────────────────┤
        └─> TT-010 ────────────────────────┤
                                           ├─> every screen in Epic E
TT-007, TT-019 ────────────────────────────┘

TT-020 ──> TT-021 (Home needs counters to show, and TT-013's cards)
TT-020 ──> TT-024 (Edit's quit variant reuses Create's quit fields)
TT-031 ──> TT-006, TT-024, TT-028  (decisions they build to)
Epic E ──> TT-027 ──> TT-029 ──> TT-030  (strings last, then the a11y pass, then release)
```

Read TT-031 first. It is four decisions and takes two minutes, and three other tickets
depend on knowing them.

## Conventions used in every ticket

- Each acceptance criterion cites the `FR-`/`NFR-` id it satisfies, so coverage is
  checkable rather than asserted.
- Visible strings are given as the **exact** design copy. Where a ticket shows a string,
  that is the string — TT-027 moves it into `strings.xml` without rewording it.
- Every ticket names the artboard and component READMEs it implements, so nobody has to
  search the canvas.

## Known gaps between the sources

Each is written into the ticket that owns it rather than left to be found mid-build:

| What | Where it is handled |
| --- | --- |
| The canvas has no quit variant of the Edit screen, but `FR-5.8` requires one | TT-024 |
| The canvas's "Delete counter" has no confirmation, but `FR-5.7` requires one | TT-024 |
| The quit screen has no route to Edit, though `FR-5.8` makes quit counters editable | TT-024 — title opens Edit; flagged for review |
| Backgrounds are accent-tinted vector shapes, not the bitmaps `FR-7.6` implies | TT-017 |
| The design hard-codes `৳` / `en-IN`, while `FR-8.3` wants the local currency | TT-027 |
| The module is `com.example.taptap` / `minSdk 26` / RC AGP, against the spec | TT-001 — now AGP 9.3.0 stable, which is also the newest Android Studio 2025.2 accepts |
| The release build type ships with optimisation disabled | TT-030 |
| Create's badge help text abbreviates the milestone list | copy kept in TT-020; the full list lives in TT-006 |
