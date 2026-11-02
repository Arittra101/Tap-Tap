# TT-017 · Counter backgrounds (Waves, Orbs, Hills)

| | |
| --- | --- |
| Epic | C · Components |
| Type | feature |
| Size | M |
| Depends on | TT-002 |

## Context

`FR-5.2` offers three default backgrounds and `FR-3.9` applies them to the counter screen
behind a scrim that keeps the number readable.

**Design note worth reading before starting.** `FR-7.6` and `NFR-9` describe these as
"bundled images … compressed WebP or vector", but the canvas does not use photos at all:
each background is a handful of SVG paths filled with the counter's **`accent`** colour at
0.18–0.45 opacity over an `accent-soft` ground. That matters — a bitmap could not re-tint
itself when the user changes the counter's colour, and the design clearly intends it to
("Default images take the counter's color"). So these are drawn, not shipped as assets:
zero bytes against the 15 MB budget and they follow the theme for free.

## Scope

**In:**
- `CounterBackground(background: Background, modifier)` drawing on a Compose `Canvas`,
  reading `accent` and `accentSoft` from the current (counter) theme:
  - **Waves** — `accentSoft` ground, three stacked bezier waves at 0.22 / 0.32 / 0.45
    opacity plus one circle at 0.18.
  - **Orbs** — `accentSoft` ground, four circles at 0.18 / 0.14 / 0.20 / 0.30.
  - **Hills** — `accentSoft` ground, one circle at 0.35 and three polyline ridges at
    0.20 / 0.32 / 0.45.
  - **None** — draws nothing; the screen keeps `bg`.
- A `scrim` layer over the drawing whenever a background is active.
- A small preview tile variant for the Edit screen's background picker.
- Paths authored against the canvas's 390×844 viewBox and scaled to the actual size.

**Out:** choosing the background — TT-024. Applying it on the counter screen — TT-022.

## Acceptance criteria

- [ ] Waves, Orbs and Hills match the canvas's shapes and opacities — `FR-5.2`.
- [ ] Each background takes the counter's own colour and re-tints when that colour
      changes — `FR-3.9`, Edit screen help text.
- [ ] A `scrim` layer sits between the background and the content so `ink` text stays
      readable — `FR-3.9`, `NFR-5`, `scrim` token usage.
- [ ] Backgrounds add no bitmap assets to the APK — `NFR-9`.
- [ ] The picker tiles are 4 options: None, Waves, Orbs, Hills, each with a content
      description — `FR-5.2`, `NFR-4`.

## Design references

`project/Counter.dc.html` → the three `<sc-if>` SVG blocks (exact paths, circles and
opacities) and the `t.scrim` overlay; `project/CounterDark.dc.html` → waves in teal-dark;
`project/Edit.dc.html` → the four picker tiles and the help line "Default images take the
counter's color. A soft layer keeps the number easy to read."

## Files

- `app/src/main/java/com/taptap/ui/components/CounterBackground.kt`

## Verification

`@Preview` of each background in two different counter colours and in light and dark, each
with a `countXl` number on top — the number must stay legible in all of them.
