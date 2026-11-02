# TT-004 · Shape and spacing tokens

| | |
| --- | --- |
| Epic | A · Foundation |
| Type | feature |
| Size | S |
| Depends on | TT-001 |

## Context

`FR-7.4` says corner sizes and spacing come from the design system, and the spec's coding
rules (§11) forbid hard-coded sizes in screens. Both scales are small and fixed.

## Scope

**In:**
- Radii from `tokens.json → radius`: `sm 8`, `md 14`, `lg 24`, `xl 28`, `pill 999`.dp,
  exposed as a `TapTapShapes` object and mapped onto `MaterialTheme.shapes`.
- Spacing from `tokens.json → spacing`: 4, 8, 12, 16, 24, 32, 48, 64 .dp, named
  `space1, space2, space3, space4, space6, space8, space12, space16`.
- `shadow-sheet` elevation values for sheets and dialogs only.
- A KDoc line per value carrying the token's `usage` text, so the next person knows that
  `space4` is screen side padding and card padding, and `space16` is the top bar.

**Out:** component geometry (a 310dp dial, a 164dp card) — those live with their
components in Epic C.

## Acceptance criteria

- [ ] Radii and spacing match `tokens.json` exactly — `FR-7.4`.
- [ ] `space12` (48dp) is documented as the minimum touch target — `NFR-4`.
- [ ] Cards get a `line` border, not a shadow; only sheets and the Home FAB carry a
      shadow — design system README, "Space, shape and layout".

## Design references

`project/tokens.json` → `radius`, `spacing`, `shadow`; `project/README.md` →
"Space, shape and layout" (390px design width, 16px side padding, sections 20–24px apart,
touch targets ≥44px and usually 48px).

## Files

- `app/src/main/java/com/taptap/ui/theme/Shape.kt`
- `app/src/main/java/com/taptap/ui/theme/Spacing.kt`

## Verification

`grep -rnE '[0-9]+\.dp' app/src/main/java/com/taptap/ui/(home|create|counter|quit|edit|settings)`
returns nothing once Epic E lands.
