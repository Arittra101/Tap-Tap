package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.habitTap.ui.theme.MinTouchTarget
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * A 40dp pill with a short label, optionally with a leading colour dot.
 *
 * Selected is `accent-soft` fill, `accent-strong` border, `on-accent-soft` text — and the
 * selectable role means the state is announced, not only drawn (NFR-6).
 */
@Composable
fun TapTapChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDotColor: Color? = null,
    enabled: Boolean = true,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            // clip before the click modifier, or the ripple draws as a rectangle
            .clip(TapTapShapes.pill)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .background(if (selected) c.accentSoft else c.surface, TapTapShapes.pill)
            .border(
                width = 1.dp,
                color = if (selected) c.accentStrong else c.lineStrong,
                shape = TapTapShapes.pill,
            )
            .padding(horizontal = Spacing.space4, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        if (leadingDotColor != null) {
            Box(Modifier.size(10.dp).background(leadingDotColor, TapTapShapes.pill))
        }
        Text(
            text = label,
            style = TapTapType.label,
            color = if (selected) c.onAccentSoft else c.ink,
        )
    }
}

/**
 * Chips laid out in a form.
 *
 * It wraps. The design system says chips in a form never scroll sideways, because a
 * horizontally scrolled option can be missed entirely.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        content()
    }
}
