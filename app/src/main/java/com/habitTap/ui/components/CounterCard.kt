package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitTap.model.Counter
import com.habitTap.util.localNumber
import com.habitTap.ui.theme.CounterTheme
import com.habitTap.ui.theme.counterAccentStrong
import com.habitTap.ui.theme.MinTouchTarget
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

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
    openLabel: String,
    targetReachedLabel: String,
    metaText: String,
    ofText: String,
    modifier: Modifier = Modifier,
) {
    val app = TapTapTheme.colors
    val isDone = counter.isTargetReached && !counter.keepCountingAfterTarget
    val hasTarget = counter.target != null && counter.target > 0
    val progress = if (hasTarget) {
        (counter.count.toFloat() / counter.target!!).coerceIn(0f, 1f)
    } else {
        0f
    }

    val cardWidth = counterCardWidth()
    val cardMinHeight = counterCardMinHeight()

    Box(modifier = modifier.width(cardWidth)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardMinHeight)
                .clip(TapTapShapes.lg)
                // FR-1.11: the whole card opens the counter. The pin and the + are their
                // own clickables, so they consume their taps and keep working.
                .clickable(role = Role.Button, onClickLabel = openLabel, onClick = onOpen)
                .background(app.surface)
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

                Text(
                    text = counter.name,
                    style = TapTapType.body.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    ),
                    color = app.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    // Keeps a two-line name clear of the floating pin.
                    modifier = Modifier.padding(end = 30.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space1)) {
                    Text(
                        text = localNumber(counter.count),
                        style = TapTapType.stat,
                        color = own.accentStrong,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = ofText,
                        style = TapTapType.label,
                        color = app.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alignByBaseline(),
                    )
                }

                // Takes up whatever height a one-line name leaves over, so the bar and the
                // + button sit at the same place on every card in the row.
                Spacer(Modifier.weight(1f))

                TapTapProgressBar(progress = progress, fillColor = own.accent)

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
                                .size(MinTouchTarget)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = addLabel
                                }
                                .clickable(
                                    role = Role.Button,
                                    indication = ripple(bounded = false, radius = 24.dp),
                                    interactionSource = null,
                                    onClick = onAdd,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(own.accent, TapTapShapes.pill),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = TapTapIcons.Plus,
                                    contentDescription = null,
                                    tint = own.onAccent,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
    }

        // Floating, so the title row stays one line tall and the touch target stays 48dp.
        TapTapIconButton(
            icon = TapTapIcons.pin(counter.isPinned),
            contentDescription = pinLabel,
            onClick = onTogglePin,
            style = IconButtonStyle.Plain,
            pressed = counter.isPinned,
            tint = if (counter.isPinned) counterAccentStrong(counter.color) else app.inkMuted,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 1.dp),
        )
    }
}

/**
 * The dashed card that ends the "Counters" row (FR-1.5).
 *
 * It takes [CounterCard]'s exact width and minimum height, so the row reads as one set of
 * cards instead of a tall row with a short stub on the end. The dashed `lineStrong` edge
 * and the `accentSoft` ground are what still mark it as an action rather than a counter.
 */
@Composable
fun NewCounterCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .width(counterCardWidth())
            .heightIn(min = counterCardMinHeight())
            .clip(TapTapShapes.lg)
            .clickable(role = Role.Button, onClick = onClick)
            .background(c.accentSoft)
            .dashedBorder(c.lineStrong, TapTapShapes.lg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Echoes the `+` on a real card, so the two are visibly the same gesture.
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(c.accent, TapTapShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TapTapIcons.Plus,
                contentDescription = null,
                tint = c.onAccent,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            style = TapTapType.label.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
            color = c.onAccentSoft,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Shared by [CounterCard] and [NewCounterCard] so the row never goes ragged. */
@Composable
private fun cardFontScale(): Float = LocalDensity.current.fontScale.coerceIn(1f, 2f)

@Composable
private fun counterCardWidth(): Dp = 164.dp * cardFontScale()

/**
 * 190dp, not the 168dp the row used to ask for: a real card's own content (name, count,
 * bar, 48dp footer) measures ~186dp, so a 168dp minimum left [NewCounterCard] visibly
 * shorter than every card beside it. Asking for a little more than the natural height
 * puts both on exactly this number.
 */
@Composable
private fun counterCardMinHeight(): Dp = 190.dp * cardFontScale()
