package com.taptap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.taptap.model.ThemeColor

/**
 * A counter's own colour.
 *
 * FR-7.3: this changes the accent tokens only — the count, progress and buttons. The
 * mode always follows the app, so `bg`, `surface` and `ink` are left exactly as
 * [TapTapTheme] set them. A null colour means "same as app" and passes straight through.
 */
@Composable
fun CounterTheme(color: ThemeColor?, content: @Composable () -> Unit) {
    if (color == null) {
        content()
        return
    }
    val dark = LocalIsDark.current
    val accents = accentSet(color, dark)
    val colors = LocalTapTapColors.current.copy(
        accent = accents.accent,
        onAccent = accents.onAccent,
        accentStrong = accents.accentStrong,
        accentSoft = accents.accentSoft,
        onAccentSoft = accents.onAccentSoft,
    )
    CompositionLocalProvider(LocalTapTapColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(dark),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}

/**
 * A counter's own accent colour, without wrapping anything in a theme.
 *
 * Useful where only the colour is needed — a chip's leading dot, a quit card's colour
 * dot — rather than a whole themed subtree. A null colour falls back to the app's accent,
 * which is what "same as app" means (FR-5.3).
 */
@Composable
fun counterAccent(color: ThemeColor?): androidx.compose.ui.graphics.Color =
    if (color == null) {
        LocalTapTapColors.current.accent
    } else {
        accentSet(color, LocalIsDark.current).accent
    }

/** A counter's own `accent-strong`, for an icon tint outside a themed subtree. */
@Composable
fun counterAccentStrong(color: ThemeColor?): androidx.compose.ui.graphics.Color =
    if (color == null) {
        LocalTapTapColors.current.accentStrong
    } else {
        accentSet(color, LocalIsDark.current).accentStrong
    }
