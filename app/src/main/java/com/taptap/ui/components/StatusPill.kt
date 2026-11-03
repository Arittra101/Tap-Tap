package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** What a [StatusPill] is saying. */
enum class PillTone {
    /** `accent-soft`: the ordinary case, e.g. "Step 5" or "Counting down". */
    Accent,

    /** `surface-sunken`: locked. */
    Muted,

    /** `success`: target reached. Always paired with a check icon. */
    Success,
}

/**
 * A 26dp pill that says a counter's state in a word.
 *
 * NFR-6 is the reason this exists at all: no state in HabitTap may be signalled by colour
 * alone, so the state always arrives as text, with an icon where it matters.
 */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tone: PillTone = PillTone.Accent,
    icon: ImageVector? = null,
) {
    val c = TapTapTheme.colors
    val container = when (tone) {
        PillTone.Accent -> c.accentSoft
        PillTone.Muted -> c.surfaceSunken
        PillTone.Success -> c.success
    }
    val content = when (tone) {
        PillTone.Accent -> c.onAccentSoft
        PillTone.Muted -> c.ink
        PillTone.Success -> c.onSuccess
    }

    Row(
        modifier = modifier
            .heightIn(min = 26.dp)
            .background(container, TapTapShapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                // The pill's own text is the label; the icon repeats it.
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp).clearAndSetSemantics { },
            )
        }
        Text(text = text, style = TapTapType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = content)
    }
}
