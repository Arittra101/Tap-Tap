package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * A `surface` group with `radius-lg`, an optional section heading, and `line`-divided
 * rows inside. Settings, the motivation options and Edit's groups all use it.
 */
@Composable
fun SettingGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable () -> Unit,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (heading != null) {
            Text(
                text = heading,
                style = TapTapType.label,
                color = c.inkMuted,
                modifier = Modifier.padding(horizontal = Spacing.space1),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surface, TapTapShapes.lg)
                .border(1.dp, c.line, TapTapShapes.lg),
        ) {
            content()
        }
    }
}

/** A hairline between two rows of a [SettingGroup]. */
@Composable
fun SettingDivider() {
    HorizontalDivider(thickness = 1.dp, color = TapTapTheme.colors.line)
}

/** A 64dp row with a switch on the right. */
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    help: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    SettingRowFrame(
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        label = label,
        help = help,
        icon = icon,
    ) {
        TapTapSwitch(checked = checked, enabled = enabled)
    }
}

/** A 64dp row showing a current value, or acting as a link. */
@Composable
fun ValueRow(
    label: String,
    modifier: Modifier = Modifier,
    help: String? = null,
    icon: ImageVector? = null,
    value: String? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = TapTapTheme.colors
    SettingRowFrame(
        modifier = if (onClick != null) {
            modifier.clickable(role = Role.Button, onClick = onClick)
        } else {
            modifier
        },
        label = label,
        help = help,
        icon = icon,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            if (value != null) {
                Text(text = value, style = TapTapType.body, color = c.inkMuted)
            }
            if (showChevron) {
                Icon(
                    imageVector = TapTapIcons.ChevronRight,
                    contentDescription = null,
                    tint = c.inkMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingRowFrame(
    label: String,
    help: String?,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Spacing.space4, vertical = Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = c.ink,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = label, style = TapTapType.body, color = c.ink)
            if (help != null) {
                Text(text = help, style = TapTapType.caption, color = c.inkMuted)
            }
        }
        trailing()
    }
}

/**
 * The switch.
 *
 * Off keeps a 2dp `line-strong` outline, which is what carries the state at 3:1 contrast
 * when the fill alone would be invisible (NFR-5).
 *
 * It draws no semantics of its own — the row around it is the toggleable target, so
 * TalkBack announces one switch rather than a row and a switch.
 */
@Composable
fun TapTapSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = TapTapTheme.colors
    val track = if (checked) c.accent else c.surfaceSunken
    val outline = if (checked) c.accent else c.lineStrong
    val knob = if (checked) c.onAccent else c.lineStrong

    Box(
        modifier = modifier
            .width(52.dp)
            .heightIn(min = 32.dp)
            .size(width = 52.dp, height = 32.dp)
            .background(track, TapTapShapes.pill)
            .border(2.dp, outline, TapTapShapes.pill),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = if (checked) 24.dp else 4.dp)
                .size(20.dp)
                .background(if (enabled) knob else c.inkMuted, TapTapShapes.pill),
        )
    }
}
