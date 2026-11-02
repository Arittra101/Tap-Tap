# TT-018 · Icon set

| | |
| --- | --- |
| Epic | C · Components |
| Type | chore |
| Size | S |
| Depends on | TT-001 |

## Context

The design system names 16 icons and says: in the Android app use the closest **Material
Symbols Rounded** at weight 400 and keep the same meanings. One mapping file stops six
screens each picking a different plus icon.

## Scope

**In:** one `TapTapIcons` object mapping each design icon to its Material Symbols Rounded
equivalent, with the meaning in a comment:

| Design icon | Used for |
| --- | --- |
| plus | add a step, the app mark, "I beat an urge", new counter |
| minus | stepper down |
| back (chevron left) | top-bar back |
| chevron right | open a counter, a settings link |
| settings (two sliders) | Home top bar |
| edit (pencil) | counter top bar |
| pin | pin to top — **filled when on** |
| focus (two circles) | make focus / your focus |
| lock | lock toggle, locked pill |
| reverse (up and down arrows) | reverse toggle |
| reset (circular arrow) | reset, "I slipped" |
| check | target reached, earned badge, selected swatch |
| quit (circle with a slash) | the quit counter type |
| vibrate | Settings |
| sound | Settings |
| shield | privacy policy |

**In also:** any icon with no good Material equivalent is added as a 24dp vector drawable
with a 2px stroke and round caps/joins, matching the design system's grid.

**Out:** the buttons that host them — TT-011.

## Acceptance criteria

- [ ] All 16 meanings are available from one file — design system README, "Iconography".
- [ ] Icons are Material Symbols Rounded at weight 400, or a 24dp/2px round-cap vector
      where none fits.
- [ ] The pin icon has a filled variant used only for the "on" state — design system README.
- [ ] No screen references a raw `Icons.Default.*` directly.

## Design references

`project/README.md` → "Iconography" (the full set and the Material mapping instruction).
The inline SVGs in each artboard show the intended shape.

## Files

- `app/src/main/java/com/taptap/ui/components/TapTapIcons.kt`
- `app/src/main/res/drawable/ic_*.xml` (only where a Material symbol does not fit)

## Verification

A `@Preview` grid of all 16 icons at 24dp in `ink`, light and dark.
