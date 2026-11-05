package com.habitTap.ui.theme

import androidx.compose.ui.unit.dp

/**
 * The spacing scale from the design system's `tokens.json` -> `spacing` (FR-7.4).
 *
 * Screens are laid out at 390dp wide with [space4] of side padding, and sections sit
 * [space6] apart.
 */
object Spacing {
    /** 4dp — icon-to-label gap, tiny nudges. */
    val space1 = 4.dp

    /** 8dp — gap between chips, padding inside small tags. */
    val space2 = 8.dp

    /** 12dp — gap between cards in a list, row padding top and bottom. */
    val space3 = 12.dp

    /** 16dp — screen side padding, card padding. */
    val space4 = 16.dp

    /** 24dp — gap between sections, sheet padding. */
    val space6 = 24.dp

    /** 32dp — space above and below the big count. */
    val space8 = 32.dp

    /** 48dp — the minimum touch target (NFR-4). Height of buttons, rows and fields. */
    val space12 = 48.dp

    /** 64dp — top bar height. */
    val space16 = 64.dp
}

/** NFR-4: nothing interactive is smaller than this. */
val MinTouchTarget = 48.dp

/** Elevation for the two things the design allows a shadow: sheets and the Home button. */
object TapTapElevation {
    val sheet = 24.dp
    val fab = 6.dp
}
