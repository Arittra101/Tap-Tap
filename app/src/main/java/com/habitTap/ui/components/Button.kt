package com.habitTap.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapElevation
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * The button variants the design system defines.
 *
 * `Danger` is deliberately limited: the design system reserves it for Reset, Delete and
 * "I slipped", and nothing else should reach for it.
 */
enum class ButtonVariant {
    /** `accent` / `on-accent`. One per screen: the main action. */
    Primary,

    /** Cancel and other quiet actions. */
    Secondary,

    /**
     * A secondary button that is currently on — "Your focus" on the quit screen.
     * `accent-soft` fill with an `accent-strong` border, per the canvas's `focusBtn`.
     */
    SecondaryActive,

    /** Only the confirm button inside a reset, slip or delete sheet. */
    Danger,

    /** The "I slipped" entry button — it asks first, it never resets at once. */
    DangerOutline,

    /** "New counter" on Home only: `ink` fill, so it stands apart from coloured cards. */
    Fab,
}

/**
 * A 52dp button with a short verb label and an optional leading icon.
 *
 * Disabled uses `surface-sunken` / `ink-muted` — that is the Create button before a name
 * is typed (FR-2.5).
 */
@Composable
fun TapTapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 52.dp,
) {
    val c = TapTapTheme.colors

    val container = when (variant) {
        ButtonVariant.Primary -> c.accent
        ButtonVariant.Secondary -> c.surface
        ButtonVariant.SecondaryActive -> c.accentSoft
        ButtonVariant.Danger -> c.danger
        ButtonVariant.DangerOutline -> c.surface
        ButtonVariant.Fab -> c.ink
    }
    val content = when (variant) {
        ButtonVariant.Primary -> c.onAccent
        ButtonVariant.Secondary -> c.ink
        ButtonVariant.SecondaryActive -> c.onAccentSoft
        ButtonVariant.Danger -> c.onDanger
        ButtonVariant.DangerOutline -> c.danger
        ButtonVariant.Fab -> c.bg
    }
    val border = when (variant) {
        ButtonVariant.Secondary, ButtonVariant.DangerOutline -> BorderStroke(1.dp, c.lineStrong)
        ButtonVariant.SecondaryActive -> BorderStroke(1.dp, c.accentStrong)
        else -> null
    }
    val shape = if (variant == ButtonVariant.Fab) TapTapShapes.pill else TapTapShapes.md
    val elevation = if (variant == ButtonVariant.Fab) {
        ButtonDefaults.buttonElevation(defaultElevation = TapTapElevation.fab)
    } else {
        ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        border = border,
        elevation = elevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            // FR-2.5: the disabled Create button.
            disabledContainerColor = c.surfaceSunken,
            disabledContentColor = c.inkMuted,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Spacing.space6,
            vertical = Spacing.space2,
        ),
        modifier = modifier.heightIn(min = minHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2, Alignment.CenterHorizontally),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    // The label beside it already says what the button does.
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                style = TapTapType.label.copy(fontSize = 15.sp, lineHeight = 20.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
