package com.taptap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner sizes from the design system's `tokens.json` -> `radius` (FR-7.4).
 *
 * The design system is explicit that cards use a `line` border rather than a shadow —
 * only bottom sheets and the Home floating button carry elevation.
 */
object TapTapShapes {
    /** 8dp — tags, small chips inside cards, thumbnails. */
    val sm = RoundedCornerShape(8.dp)

    /** 14dp — text fields, buttons, settings groups. */
    val md = RoundedCornerShape(14.dp)

    /** 24dp — counter cards, stat strips, setting groups. */
    val lg = RoundedCornerShape(24.dp)

    /** 28dp — the Focus card, the dashed "pick a focus" card, sheet top corners. */
    val xl = RoundedCornerShape(28.dp)

    /** Fully round — chips, the segmented control, switches, the tap pad. */
    val pill = RoundedCornerShape(percent = 50)

    /** A bottom sheet: `xl` on top, square where it meets the screen edge. */
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

/** Material 3's shape scale, mapped onto the design's radii. */
val TapTapMaterialShapes = Shapes(
    extraSmall = TapTapShapes.sm,
    small = TapTapShapes.sm,
    medium = TapTapShapes.md,
    large = TapTapShapes.lg,
    extraLarge = TapTapShapes.xl,
)
