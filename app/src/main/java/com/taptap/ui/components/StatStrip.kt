package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** One cell of a [StatStrip]: a value over a label. */
data class Stat(val value: String, val label: String)

/**
 * Exactly three cells in one `surface` card, split by `line` dividers.
 *
 * Normal counter: Today / Left to target / Done. Quit counter: Money saved / Urges beaten
 * / Best streak. The design system is specific that it is always three — a strip with two
 * or four would not read as the same component.
 */
@Composable
fun StatStrip(
    stats: List<Stat>,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = TapTapType.statValue.copy(fontSize = 22.sp, lineHeight = 28.sp),
) {
    require(stats.size == 3) { "StatStrip takes exactly three stats, got ${stats.size}" }
    val c = TapTapTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface, TapTapShapes.lg)
            .border(1.dp, c.line, TapTapShapes.lg)
            .padding(vertical = Spacing.space3, horizontal = Spacing.space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    thickness = 1.dp,
                    color = c.line,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.space1)
                    // One announcement per cell: "+12, Today".
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${stat.value}, ${stat.label}"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stat.value,
                    style = valueStyle,
                    color = c.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clearAndSetSemantics { },
                )
                Text(
                    text = stat.label,
                    style = TapTapType.caption,
                    color = c.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
    }
}
