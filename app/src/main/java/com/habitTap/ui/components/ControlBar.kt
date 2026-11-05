package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * Lock, Reverse and Reset in one `surface` bar below the dial.
 *
 * Lock and Reverse are toggles whose **labels change with state** — "Lock" becomes
 * "Unlock", "Reverse" becomes "Count up". That is the design system's rule and it is also
 * what keeps the state readable without relying on the `accent-soft` fill (NFR-6).
 *
 * Reset never acts here. It raises [onResetClick], and the screen opens a sheet first
 * (FR-3.6).
 */
@Composable
fun ControlBar(
    isLocked: Boolean,
    isReversed: Boolean,
    lockLabel: String,
    reverseLabel: String,
    resetLabel: String,
    onToggleLock: () -> Unit,
    onToggleReverse: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface, TapTapShapes.lg)
            .border(1.dp, c.line, TapTapShapes.lg)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ControlCell(
            icon = TapTapIcons.lock(isLocked),
            label = lockLabel,
            modifier = Modifier
                .weight(1f)
                .clip(TapTapShapes.md)
                .toggleable(
                    value = isLocked,
                    role = Role.Switch,
                    onValueChange = { onToggleLock() },
                ),
            on = isLocked,
        )
        ControlCell(
            icon = TapTapIcons.Reverse,
            label = reverseLabel,
            modifier = Modifier
                .weight(1f)
                .clip(TapTapShapes.md)
                .toggleable(
                    value = isReversed,
                    role = Role.Switch,
                    onValueChange = { onToggleReverse() },
                ),
            on = isReversed,
        )
        ControlCell(
            icon = TapTapIcons.Reset,
            label = resetLabel,
            modifier = Modifier
                .weight(1f)
                .clip(TapTapShapes.md)
                .clickable(role = Role.Button, onClick = onResetClick),
            on = false,
            contentColor = c.danger,
        )
    }
}

@Composable
private fun ControlCell(
    icon: ImageVector,
    label: String,
    on: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color? = null,
) {
    val c = TapTapTheme.colors
    val content = contentColor ?: if (on) c.onAccentSoft else c.ink
    Column(
        modifier = modifier
            .heightIn(min = 60.dp)
            .background(
                if (on) c.accentSoft else Color.Transparent,
                TapTapShapes.md,
            )
            .padding(vertical = Spacing.space2, horizontal = Spacing.space1),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space1, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = TapTapType.caption.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
            color = content,
            textAlign = TextAlign.Center,
        )
    }
}
