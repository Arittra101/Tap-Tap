package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.habitTap.ui.theme.MinTouchTarget
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme

/** How an [TapTapIconButton] is framed. */
enum class IconButtonStyle {
    /** 48dp, `surface` fill with a `line` border. Used in the top bar. */
    Framed,

    /** 40dp, no fill. Used for pin and focus inside cards. */
    Plain,
}

/**
 * A round button with only an icon.
 *
 * [contentDescription] is required and non-null: the design system says icon-only buttons
 * always carry a text label for screen readers, and NFR-4 makes it a release criterion.
 *
 * [pressed] turns the icon `accentStrong` — that is the pin's "on" state. Passing it also
 * exposes the button as toggleable to accessibility services, so the state is announced
 * rather than only drawn.
 */
@Composable
fun TapTapIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconButtonStyle = IconButtonStyle.Framed,
    pressed: Boolean? = null,
    enabled: Boolean = true,
    tint: Color? = null,
) {
    val c = TapTapTheme.colors
    val visualSize = if (style == IconButtonStyle.Framed) 48.dp else 40.dp
    val iconSize = if (style == IconButtonStyle.Framed) 24.dp else 20.dp
    val resolvedTint = tint ?: when {
        !enabled -> c.inkMuted
        pressed == true -> c.accentStrong
        else -> c.ink
    }

    /*
     * The label lives on this outer box, not on the Icon.
     *
     * With it on the Icon, the node an accessibility service sees is the 20dp glyph —
     * the touch area was 48dp but the reported target was not, which fails NFR-4 for
     * anyone using touch exploration or an a11y scanner. Merging here makes the
     * announced node the same size as the thing you can actually press.
     */
    val buttonSemantics = Modifier.semantics(mergeDescendants = true) {
        this.contentDescription = contentDescription
        if (pressed != null) {
            toggleableState = if (pressed) ToggleableState.On else ToggleableState.Off
        }
    }

    Box(
        modifier = modifier
            // NFR-4: the 40dp plain variant still gets a 48dp touch area.
            .sizeIn(minWidth = MinTouchTarget, minHeight = MinTouchTarget)
            .then(buttonSemantics)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
                indication = ripple(bounded = false, radius = visualSize / 2),
                interactionSource = remember { MutableInteractionSource() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(visualSize)
                .then(
                    if (style == IconButtonStyle.Framed) {
                        Modifier
                            .background(c.surface, TapTapShapes.pill)
                            .border(1.dp, c.line, TapTapShapes.pill)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                // The label is on the button above.
                contentDescription = null,
                tint = resolvedTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
