package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taptap.model.Counter
import com.taptap.util.localNumber
import com.taptap.ui.theme.CounterTheme
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * One normal counter in Home's horizontal "Counters" row (FR-1.6).
 *
 * The card frame stays the app's `surface`; only the count, the progress fill and the
 * `+` take the counter's own colour. That is FR-7.3, and it is why the contents are
 * wrapped in [CounterTheme] rather than the whole card being repainted.
 */
@Composable
fun CounterCard(
    counter: Counter,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onTogglePin: () -> Unit,
    addLabel: String,
    pinLabel: String,
    targetReachedLabel: String,
    metaText: String,
    ofText: String,
    modifier: Modifier = Modifier,
) {
    val app = TapTapTheme.colors
    val isDone = counter.isTargetReached && !counter.keepCountingAfterTarget
    val progress = counter.target?.takeIf { it > 0 }
        ?.let { (counter.count.toFloat() / it).coerceIn(0f, 1f) } ?: 1f

    val cardWidth = 164.dp * LocalDensity.current.fontScale.coerceIn(1f, 2f)

    Column(
        modifier = modifier
            .width(cardWidth)
            .background(app.surface, TapTapShapes.lg)
            .border(
                width = 1.dp,
                // Pinned cards carry a stronger edge as well as a filled pin (NFR-6).
                color = if (counter.isPinned) app.lineStrong else app.line,
                shape = TapTapShapes.lg,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CounterTheme(counter.color) {
            val own = TapTapTheme.colors

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.heightIn(min = 40.dp),
            ) {
                Text(
                    text = counter.name,
                    style = TapTapType.body.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    ),
                    color = app.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clip(TapTapShapes.sm)
                        .clickable(role = Role.Button, onClick = onOpen),
                )
                TapTapIconButton(
                    icon = TapTapIcons.pin(counter.isPinned),
                    contentDescription = pinLabel,
                    onClick = onTogglePin,
                    style = IconButtonStyle.Plain,
                    pressed = counter.isPinned,
                    tint = if (counter.isPinned) own.accentStrong else app.inkMuted,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
            ) {
                Text(
                    text = localNumber(counter.count),
                    style = TapTapType.stat,
                    color = own.accentStrong,
                )
                Text(text = ofText, style = TapTapType.label, color = app.inkMuted)
            }

            TapTapProgressBar(
                progress = progress,
                fillColor = if (counter.target != null) own.accent else own.accentSoft,
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = metaText,
                    style = TapTapType.caption,
                    color = app.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (isDone) {
                    // FR-1.6 / NFR-6: at the target the + becomes a check, and the check
                    // carries the words "Target reached" for anyone not seeing the colour.
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(app.success, TapTapShapes.pill),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TapTapIcons.Check,
                            contentDescription = targetReachedLabel,
                            tint = app.onSuccess,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(TapTapShapes.pill)
                            .background(own.accent)
                            .clickable(role = Role.Button, onClick = onAdd),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TapTapIcons.Plus,
                            contentDescription = addLabel,
                            tint = own.onAccent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

/** The dashed card that ends the "Counters" row (FR-1.5). */
@Composable
fun NewCounterCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .widthIn(min = 120.dp)
            .dashedBorder(c.lineStrong, TapTapShapes.lg)
            .clip(TapTapShapes.lg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = TapTapIcons.Plus,
            contentDescription = null,
            tint = c.ink,
            modifier = Modifier.size(24.dp),
        )
        Text(text = label, style = TapTapType.label, color = c.ink)
    }
}
