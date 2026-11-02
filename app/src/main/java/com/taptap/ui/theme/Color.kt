package com.taptap.ui.theme

import androidx.compose.ui.graphics.Color
import com.taptap.model.ThemeColor

/**
 * Every colour Tap Tap draws.
 *
 * The values come from the design system's `project/tokens.json` and are copied, never
 * computed — FR-7.4 forbids hard-coded colours anywhere else in the app, which only works
 * if this file is the one place they live.
 *
 * A theme is one colour x one mode, so there are eight of them (FR-7.1). The neutral
 * tokens are identical across the four colours and differ only by mode, so they are
 * declared once per mode; only the five accent tokens change with the colour. This is the
 * same split the design canvas uses (`TT_N` / `TT_A`).
 */
data class TapTapColors(
    /** Screen background behind everything. */
    val bg: Color,
    /** Cards, list rows, the tap pad, bottom bars. Sits on [bg]. */
    val surface: Color,
    /** Bottom sheets, dialogs, menus — anything floating above [surface]. */
    val surfaceRaised: Color,
    /** Text field fill, progress track, segmented control track, unselected chip fill. */
    val surfaceSunken: Color,
    /** Main text and icons. Passes 4.5:1 on bg, surface, surfaceRaised, surfaceSunken. */
    val ink: Color,
    /** Secondary text: notes, captions, helper text, units. */
    val inkMuted: Color,
    /** Hairline dividers and quiet card borders. Never the only edge of a control. */
    val line: Color,
    /** Edges that carry meaning: field borders, chip borders, switch outlines (3:1). */
    val lineStrong: Color,
    /** The colour theme. Primary buttons, tap pad, progress ring, selected chip, switch on. */
    val accent: Color,
    /** Text and icons placed on [accent]. */
    val onAccent: Color,
    /** Accent as text or an icon on bg/surface. Pressed state of accent fills in light mode. */
    val accentStrong: Color,
    /** Tinted fill: selected row, selected chip background, a tag on a counter card. */
    val accentSoft: Color,
    /** Text and icons on [accentSoft]. */
    val onAccentSoft: Color,
    /** Reset and delete only, always with a word or icon (never colour alone). */
    val danger: Color,
    /** Text on a [danger] fill. */
    val onDanger: Color,
    /** "Target reached" and streak-kept badges, always with a check icon or word. */
    val success: Color,
    /** Text on a [success] fill. */
    val onSuccess: Color,
    /** Layer between a counter's background and its text, so [ink] stays readable. */
    val scrim: Color,
    /** Keyboard / D-pad focus ring, 2dp solid, 2dp offset. Alias of [ink]. */
    val focus: Color,
    /** Backdrop behind bottom sheets. Not a text colour. */
    val dim: Color,
    /** The pink theme shown as a swatch, and as a counter's own colour dot. */
    val swatchPink: Color,
    /** The teal theme shown as a swatch, and as a counter's own colour dot. */
    val swatchTeal: Color,
    /** The saffron theme shown as a swatch, and as a counter's own colour dot. */
    val swatchSaffron: Color,
    /** The blue theme shown as a swatch, and as a counter's own colour dot. */
    val swatchBlue: Color,
)

// ---------------------------------------------------------------------------------------
// Neutral tokens: the same in every colour theme, different per mode.
// ---------------------------------------------------------------------------------------

private val LightBg = Color(0xFFF7F5F2)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceRaised = Color(0xFFFFFFFF)
private val LightSurfaceSunken = Color(0xFFEEEBE6)
private val LightInk = Color(0xFF1C1A18)
private val LightInkMuted = Color(0xFF5C5751)
private val LightLine = Color(0xFFD9D4CC)
private val LightLineStrong = Color(0xFF8A847C)
private val LightDanger = Color(0xFFB3261E)
private val LightOnDanger = Color(0xFFFFFFFF)
private val LightSuccess = Color(0xFF2E6B34)
private val LightOnSuccess = Color(0xFFFFFFFF)
private val LightScrim = Color(0xFFF7F5F2).copy(alpha = 0.80f)
private val LightDim = Color(0xFF1C1A18).copy(alpha = 0.45f)

private val DarkBg = Color(0xFF121110)
private val DarkSurface = Color(0xFF1C1B19)
private val DarkSurfaceRaised = Color(0xFF272522)
private val DarkSurfaceSunken = Color(0xFF0B0A09)
private val DarkInk = Color(0xFFF2EFEA)
private val DarkInkMuted = Color(0xFFAAA39A)
private val DarkLine = Color(0xFF38352F)
private val DarkLineStrong = Color(0xFF7C766E)
private val DarkDanger = Color(0xFFFFB4AB)
private val DarkOnDanger = Color(0xFF5A0B05)
private val DarkSuccess = Color(0xFF93D39A)
private val DarkOnSuccess = Color(0xFF0B3811)
private val DarkScrim = Color(0xFF121110).copy(alpha = 0.74f)
private val DarkDim = Color(0xFF000000).copy(alpha = 0.60f)

// ---------------------------------------------------------------------------------------
// Accent tokens: the five that change with the colour theme, and with a counter's own
// colour (FR-7.3).
// ---------------------------------------------------------------------------------------

/** The five accent tokens for one colour x one mode. */
internal data class AccentSet(
    val accent: Color,
    val onAccent: Color,
    val accentStrong: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
)

private val PinkLightAccents = AccentSet(
    accent = Color(0xFFC2185B),
    onAccent = Color(0xFFFFFFFF),
    accentStrong = Color(0xFF96124A),
    accentSoft = Color(0xFFFCE4EC),
    onAccentSoft = Color(0xFF7F0F3E),
)
private val PinkDarkAccents = AccentSet(
    accent = Color(0xFFF48FB1),
    onAccent = Color(0xFF3D0A1E),
    accentStrong = Color(0xFFF8BBD0),
    accentSoft = Color(0xFF3B1A27),
    onAccentSoft = Color(0xFFF8BBD0),
)
private val TealLightAccents = AccentSet(
    accent = Color(0xFF00796B),
    onAccent = Color(0xFFFFFFFF),
    accentStrong = Color(0xFF005A4F),
    accentSoft = Color(0xFFDDF2EE),
    onAccentSoft = Color(0xFF00463E),
)
private val TealDarkAccents = AccentSet(
    accent = Color(0xFF4FD1BF),
    onAccent = Color(0xFF00302A),
    accentStrong = Color(0xFF91E7DA),
    accentSoft = Color(0xFF12302C),
    onAccentSoft = Color(0xFF91E7DA),
)
private val SaffronLightAccents = AccentSet(
    accent = Color(0xFFB45309),
    onAccent = Color(0xFFFFFFFF),
    accentStrong = Color(0xFF8A3F06),
    accentSoft = Color(0xFFFDEEDC),
    onAccentSoft = Color(0xFF743405),
)
private val SaffronDarkAccents = AccentSet(
    accent = Color(0xFFF6B25B),
    onAccent = Color(0xFF3A2000),
    accentStrong = Color(0xFFFAD29C),
    accentSoft = Color(0xFF352619),
    onAccentSoft = Color(0xFFFAD29C),
)
private val BlueLightAccents = AccentSet(
    accent = Color(0xFF1F5FBF),
    onAccent = Color(0xFFFFFFFF),
    accentStrong = Color(0xFF174A96),
    accentSoft = Color(0xFFE3ECFA),
    onAccentSoft = Color(0xFF123D7C),
)
private val BlueDarkAccents = AccentSet(
    accent = Color(0xFF8AB4F8),
    onAccent = Color(0xFF0A2A5C),
    accentStrong = Color(0xFFB9D2FB),
    accentSoft = Color(0xFF1B2638),
    onAccentSoft = Color(0xFFB9D2FB),
)

/** The accent set for a colour x mode. Also used by [CounterTheme] for a counter's own colour. */
internal fun accentSet(color: ThemeColor, dark: Boolean): AccentSet = when (color) {
    ThemeColor.PINK -> if (dark) PinkDarkAccents else PinkLightAccents
    ThemeColor.TEAL -> if (dark) TealDarkAccents else TealLightAccents
    ThemeColor.SAFFRON -> if (dark) SaffronDarkAccents else SaffronLightAccents
    ThemeColor.BLUE -> if (dark) BlueDarkAccents else BlueLightAccents
}

// ---------------------------------------------------------------------------------------
// The eight themes.
// ---------------------------------------------------------------------------------------

private fun colors(dark: Boolean, accents: AccentSet): TapTapColors = TapTapColors(
    bg = if (dark) DarkBg else LightBg,
    surface = if (dark) DarkSurface else LightSurface,
    surfaceRaised = if (dark) DarkSurfaceRaised else LightSurfaceRaised,
    surfaceSunken = if (dark) DarkSurfaceSunken else LightSurfaceSunken,
    ink = if (dark) DarkInk else LightInk,
    inkMuted = if (dark) DarkInkMuted else LightInkMuted,
    line = if (dark) DarkLine else LightLine,
    lineStrong = if (dark) DarkLineStrong else LightLineStrong,
    accent = accents.accent,
    onAccent = accents.onAccent,
    accentStrong = accents.accentStrong,
    accentSoft = accents.accentSoft,
    onAccentSoft = accents.onAccentSoft,
    danger = if (dark) DarkDanger else LightDanger,
    onDanger = if (dark) DarkOnDanger else LightOnDanger,
    success = if (dark) DarkSuccess else LightSuccess,
    onSuccess = if (dark) DarkOnSuccess else LightOnSuccess,
    scrim = if (dark) DarkScrim else LightScrim,
    // `focus` is an alias of `ink` in tokens.json.
    focus = if (dark) DarkInk else LightInk,
    dim = if (dark) DarkDim else LightDim,
    // Swatches look the same in every colour theme and only follow light/dark.
    swatchPink = if (dark) PinkDarkAccents.accent else PinkLightAccents.accent,
    swatchTeal = if (dark) TealDarkAccents.accent else TealLightAccents.accent,
    swatchSaffron = if (dark) SaffronDarkAccents.accent else SaffronLightAccents.accent,
    swatchBlue = if (dark) BlueDarkAccents.accent else BlueLightAccents.accent,
)

val PinkLight: TapTapColors = colors(dark = false, accents = PinkLightAccents)
val PinkDark: TapTapColors = colors(dark = true, accents = PinkDarkAccents)
val TealLight: TapTapColors = colors(dark = false, accents = TealLightAccents)
val TealDark: TapTapColors = colors(dark = true, accents = TealDarkAccents)
val SaffronLight: TapTapColors = colors(dark = false, accents = SaffronLightAccents)
val SaffronDark: TapTapColors = colors(dark = true, accents = SaffronDarkAccents)
val BlueLight: TapTapColors = colors(dark = false, accents = BlueLightAccents)
val BlueDark: TapTapColors = colors(dark = true, accents = BlueDarkAccents)

/** The palette for one of the eight app themes (FR-7.1). */
fun tapTapColors(color: ThemeColor, dark: Boolean): TapTapColors = when (color) {
    ThemeColor.PINK -> if (dark) PinkDark else PinkLight
    ThemeColor.TEAL -> if (dark) TealDark else TealLight
    ThemeColor.SAFFRON -> if (dark) SaffronDark else SaffronLight
    ThemeColor.BLUE -> if (dark) BlueDark else BlueLight
}

/** The swatch colour for a theme colour, for the Settings and Edit pickers. */
fun TapTapColors.swatch(color: ThemeColor): Color = when (color) {
    ThemeColor.PINK -> swatchPink
    ThemeColor.TEAL -> swatchTeal
    ThemeColor.SAFFRON -> swatchSaffron
    ThemeColor.BLUE -> swatchBlue
}
