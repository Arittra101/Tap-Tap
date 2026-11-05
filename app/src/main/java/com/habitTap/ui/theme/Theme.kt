package com.habitTap.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode

/** The palette in force. Read it through [TapTapTheme.colors]. */
val LocalTapTapColors = staticCompositionLocalOf { PinkLight }

/**
 * Whether the app is in dark mode. [CounterTheme] needs this: a counter's own colour
 * swaps the accents but never the mode (FR-7.3).
 */
val LocalIsDark = staticCompositionLocalOf { false }

/**
 * The app theme: one colour x one mode (FR-7.1).
 *
 * [ThemeMode.SYSTEM] is not a theme of its own — it reads the phone's setting and picks
 * the light or dark theme of the chosen colour, and follows along when the phone setting
 * changes (FR-7.2).
 */
@Composable
fun TapTapTheme(
    color: ThemeColor = ThemeColor.PINK,
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = tapTapColors(color, dark)

    /*
     * The status and navigation bar icons follow the APP's mode, not the system's.
     *
     * enableEdgeToEdge() decides this once at start-up from the system configuration, so
     * with the phone in dark mode and the app set to Light the icons stayed light on a
     * light background and vanished (and the reverse). FR-6.2 lets the two disagree, so
     * the bars have to be told which one actually won.
     */
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        SideEffect {
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
                /*
                 * The window background too. `Theme.TapTap` descends from
                 * Theme.AppCompat.DayNight, so its `tt_bg` resolves against the SYSTEM
                 * night setting — with the phone dark and the app set to Light, every
                 * pixel Compose has not painted yet (start-up, recreation, overscroll)
                 * came out dark. FR-6.2 lets the two disagree, so the window has to be
                 * told which one won.
                 */
                window.setBackgroundDrawable(ColorDrawable(colors.bg.toArgb()))
            }
        }
    }

    CompositionLocalProvider(
        LocalTapTapColors provides colors,
        LocalIsDark provides dark,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(dark),
            typography = TapTapTypography,
            shapes = TapTapMaterialShapes,
            content = content,
        )
    }
}

/**
 * Short accessor so screens read `TapTapTheme.colors.accent` instead of reaching for the
 * composition local directly.
 */
object TapTapTheme {
    val colors: TapTapColors
        @Composable @ReadOnlyComposable get() = LocalTapTapColors.current

    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalIsDark.current
}

/**
 * Material 3 components that HabitTap uses need a scheme too. The mapping keeps Material's
 * defaults in step with the design tokens so a stray Material component never paints
 * itself an off-brand colour.
 */
fun TapTapColors.toMaterialScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentSoft,
        onPrimaryContainer = onAccentSoft,
        secondary = accentStrong,
        onSecondary = onAccent,
        secondaryContainer = accentSoft,
        onSecondaryContainer = onAccentSoft,
        tertiary = accentStrong,
        onTertiary = onAccent,
        background = bg,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = inkMuted,
        surfaceContainer = surfaceRaised,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerLow = surface,
        outline = lineStrong,
        outlineVariant = line,
        error = danger,
        onError = onDanger,
        scrim = dim,
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentSoft,
        onPrimaryContainer = onAccentSoft,
        secondary = accentStrong,
        onSecondary = onAccent,
        secondaryContainer = accentSoft,
        onSecondaryContainer = onAccentSoft,
        tertiary = accentStrong,
        onTertiary = onAccent,
        background = bg,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = inkMuted,
        surfaceContainer = surfaceRaised,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerLow = surface,
        outline = lineStrong,
        outlineVariant = line,
        error = danger,
        onError = onDanger,
        scrim = dim,
    )
}
