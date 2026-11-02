package com.taptap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** The 168dp ring illustration above the first-run welcome (FR-1.10). */
@Composable
fun WelcomeMark(modifier: Modifier = Modifier) {
    val c = TapTapTheme.colors
    Canvas(modifier.size(168.dp).clearAndSetSemantics { }) {
        val stroke = 14.dp.toPx()
        val r = size.minDimension / 2f - stroke / 2f
        val arcSize = Size(r * 2, r * 2)
        val topLeft = Offset(center.x - r, center.y - r)
        drawArc(c.surfaceSunken, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        drawArc(
            c.accent, -90f, 119f, false, topLeft, arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawCircle(c.accentSoft, radius = size.minDimension * 0.286f)
        val arm = size.minDimension * 0.107f
        val plus = Stroke(7.dp.toPx(), cap = StrokeCap.Round)
        drawLine(c.accentStrong, center.copy(y = center.y - arm), center.copy(y = center.y + arm), plus.width, StrokeCap.Round)
        drawLine(c.accentStrong, center.copy(x = center.x - arm), center.copy(x = center.x + arm), plus.width, StrokeCap.Round)
    }
}

/** One of the two big choices on first run (FR-1.10). */
@Composable
fun BigChoiceRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentFill: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface, TapTapShapes.lg)
            .border(1.dp, c.line, TapTapShapes.lg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (accentFill) c.accent else c.accentSoft, TapTapShapes.md),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (accentFill) c.onAccent else c.onAccentSoft,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = title, style = TapTapType.title, color = c.ink)
            Text(text = subtitle, style = TapTapType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = c.inkMuted)
        }
        Icon(
            imageVector = TapTapIcons.ChevronRight,
            contentDescription = null,
            tint = c.inkMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}
