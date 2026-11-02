package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** One option in a [SegmentedControl]. */
data class Segment<T>(val value: T, val label: String)

/**
 * A `surface-sunken` pill of 2-3 equal options; the chosen one is a raised `surface` pill
 * with a `line-strong` outline.
 *
 * Used for Mode (Light / Dark / System) and the quit date (Today / Earlier date).
 */
@Composable
fun <T> SegmentedControl(
    segments: List<Segment<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surfaceSunken, TapTapShapes.pill)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        segments.forEach { segment ->
            val isSelected = segment.value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(TapTapShapes.pill)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(segment.value) },
                    )
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(c.surface, TapTapShapes.pill)
                                .border(1.dp, c.lineStrong, TapTapShapes.pill)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = segment.label,
                    style = TapTapType.label,
                    color = if (isSelected) c.ink else c.inkMuted,
                )
            }
        }
    }
}
