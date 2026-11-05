package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * A 52dp ring with the colour inside; selected gets an `ink` ring and a check.
 *
 * The colour's name is always shown beneath it. The design system insists on that, and
 * NFR-6 requires it: a swatch row that only differs by hue is unusable for anyone who
 * cannot tell the hues apart.
 *
 * [fill] of null is the "App" option in Edit counter — same colour as the app.
 */
@Composable
fun ColorSwatch(
    name: String,
    fill: Color?,
    onFill: Color?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    appOptionLabel: String? = null,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .clip(TapTapShapes.md)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            // The visible name below is cleared, so the name has to live here or the
            // swatch would be an unlabelled radio button (NFR-4).
            .semantics(mergeDescendants = true) { contentDescription = name }
            .padding(vertical = Spacing.space1),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .border(
                    width = 2.dp,
                    color = if (selected) c.ink else Color.Transparent,
                    shape = TapTapShapes.pill,
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fill ?: c.surfaceSunken, TapTapShapes.pill)
                    .then(
                        if (fill == null) {
                            Modifier.border(1.dp, c.lineStrong, TapTapShapes.pill)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    selected -> Icon(
                        imageVector = TapTapIcons.Check,
                        contentDescription = null,
                        tint = onFill ?: c.ink,
                        modifier = Modifier.size(20.dp),
                    )
                    fill == null && appOptionLabel != null -> Text(
                        text = appOptionLabel,
                        style = TapTapType.caption,
                        color = c.ink,
                    )
                }
            }
        }
        Text(
            text = name,
            style = TapTapType.caption,
            color = c.ink,
            textAlign = TextAlign.Center,
            // The row above is the selectable target and already announces the name.
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
