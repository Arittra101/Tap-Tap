package com.taptap.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The design system's 16 icons, mapped to Material Symbols Rounded.
 *
 * The design system README says to use "the closest Material Symbols Rounded icons at
 * weight 400 and keep the same meanings". Doing that in one file is what stops six
 * screens each picking a different plus.
 *
 * Icons are named for what they *mean* here, not for the glyph, so a later swap changes
 * one line rather than every call site.
 */
object TapTapIcons {

    /** Add a step; the app mark; "I beat an urge"; new counter. */
    val Plus: ImageVector get() = Icons.Rounded.Add

    /** Stepper down. */
    val Minus: ImageVector get() = Icons.Rounded.Remove

    /** Top-bar back. Auto-mirrored so it still points "back" in an RTL layout. */
    val Back: ImageVector get() = Icons.AutoMirrored.Rounded.KeyboardArrowLeft

    /** Open a counter; a settings link. */
    val ChevronRight: ImageVector get() = Icons.AutoMirrored.Rounded.KeyboardArrowRight

    /** Settings, in the Home top bar. The design draws this as two sliders. */
    val Settings: ImageVector get() = Icons.Rounded.Tune

    /** Edit, in the counter top bar. */
    val Edit: ImageVector get() = Icons.Rounded.Edit

    /**
     * Pin to top, off.
     *
     * Outlined, not Rounded: Material's Rounded PushPin is a filled glyph, and the design
     * system reserves the filled pin for the "on" state alone.
     */
    val Pin: ImageVector get() = Icons.Outlined.PushPin

    /** Pin to top, on. The design system fills the pin only in the "on" state. */
    val PinFilled: ImageVector get() = Icons.Filled.PushPin

    /** Make focus / your focus. The design draws this as two concentric circles. */
    val Focus: ImageVector get() = Icons.Rounded.Adjust

    /** Lock toggle, on; the locked pill on the dial. */
    val Lock: ImageVector get() = Icons.Rounded.Lock

    /** Lock toggle, off. */
    val Unlock: ImageVector get() = Icons.Rounded.LockOpen

    /** Reverse toggle: up and down arrows. */
    val Reverse: ImageVector get() = Icons.Rounded.SwapVert

    /** Reset, and "I slipped": a circular arrow. */
    val Reset: ImageVector get() = Icons.Rounded.Refresh

    /** Target reached; an earned badge; a selected swatch. */
    val Check: ImageVector get() = Icons.Rounded.Check

    /** The quit counter type: a circle with a slash. */
    val Quit: ImageVector get() = Icons.Rounded.Block

    /** Vibration, in Settings. */
    val Vibrate: ImageVector get() = Icons.Rounded.Vibration

    /** Sound, in Settings. */
    val Sound: ImageVector get() = Icons.AutoMirrored.Rounded.VolumeUp

    /** Privacy policy. */
    val Shield: ImageVector get() = Icons.Rounded.Shield

    /** The pin icon for a given state, so call sites do not branch. */
    fun pin(isPinned: Boolean): ImageVector = if (isPinned) PinFilled else Pin

    /** The lock icon for a given state. */
    fun lock(isLocked: Boolean): ImageVector = if (isLocked) Lock else Unlock
}
