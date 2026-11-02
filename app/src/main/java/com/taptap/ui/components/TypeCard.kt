package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * One of the two big cards at the top of New counter (FR-2.1).
 *
 * The choice decides which fields the form shows below, so it is deliberately the largest
 * thing on the screen.
 */
@Composable
fun TypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .heightIn(min = 150.dp)
            .clip(TapTapShapes.lg)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(if (selected) c.accentSoft else c.surface, TapTapShapes.lg)
            .border(
                width = 2.dp,
                color = if (selected) c.accentStrong else c.line,
                shape = TapTapShapes.lg,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (selected) c.accent else c.surfaceSunken,
                    TapTapShapes.md,
                )
                .padding(bottom = 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) c.onAccent else c.ink,
                modifier = Modifier.size(22.dp),
            )
        }
        Box(Modifier.size(Spacing.space1))
        Text(
            text = title,
            style = TapTapType.title,
            color = if (selected) c.onAccentSoft else c.ink,
        )
        Text(
            text = subtitle,
            style = TapTapType.caption,
            color = if (selected) c.onAccentSoft else c.inkMuted,
        )
    }
}
