package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habitTap.model.Counter
import com.habitTap.ui.theme.counterAccent
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * Shown in the Focus card's place when quit counters exist but none is the focus
 * (FR-1.3). Tapping a chip makes that counter the focus.
 *
 * When there are no quit counters at all this is not shown either — the whole focus area
 * disappears (FR-1.4). That decision belongs to Home, not here.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusPicker(
    quitCounters: List<Counter>,
    title: String,
    help: String,
    onPick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface, TapTapShapes.xl)
            .dashedBorder(c.lineStrong, TapTapShapes.xl)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(c.accentSoft, TapTapShapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TapTapIcons.Focus,
                    contentDescription = null,
                    tint = c.onAccentSoft,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
                Text(text = title, style = TapTapType.title, color = c.ink)
                Text(text = help, style = TapTapType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = c.inkMuted)
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            quitCounters.forEach { counter ->
                TapTapChip(
                    label = counter.name,
                    selected = false,
                    onClick = { onPick(counter.id) },
                    leadingDotColor = counterAccent(counter.color),
                )
            }
        }
    }
}
