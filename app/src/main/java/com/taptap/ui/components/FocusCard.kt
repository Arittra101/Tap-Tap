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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taptap.R
import com.taptap.model.Counter
import com.taptap.util.localNumber
import com.taptap.ui.theme.CounterTheme
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * The one quit counter the user chose to focus on, first on Home (FR-1.2).
 *
 * The whole card is filled with that counter's `accent` and every piece of text is
 * `on-accent` — so unlike the other cards, this one *is* repainted in the counter's
 * colour. The progress track is `accent-strong` with an `on-accent` fill.
 *
 * It carries exactly one button. The design system is explicit about that: the chevron
 * opens the quit screen, and "I beat an urge" is the only action.
 */
@Composable
fun FocusCard(
    counter: Counter,
    daysFree: Int,
    milestoneProgress: Float,
    nextBadgeText: String,
    moneySavedText: String,
    urgesTodayText: String,
    sectionLabels: FocusCardLabels,
    onOpen: () -> Unit,
    onBeatUrge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CounterTheme(counter.color) {
        val own = TapTapTheme.colors
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(own.accent, TapTapShapes.xl)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    // FR-1.11: tapping the name opens the counter, same as the chevron.
                    // Not a second button — the design system's rule is about actions.
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button, onClick = onOpen),
                ) {
                    Icon(
                        imageVector = TapTapIcons.Focus,
                        contentDescription = null,
                        tint = own.onAccent,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = counter.name,
                        style = TapTapType.label,
                        color = own.onAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TapTapIconButton(
                    icon = TapTapIcons.ChevronRight,
                    contentDescription = sectionLabels.open,
                    onClick = onOpen,
                    style = IconButtonStyle.Plain,
                    tint = own.onAccent,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(text = localNumber(daysFree), style = TapTapType.countLg, color = own.onAccent)
                Text(
                    text = sectionLabels.daysFree,
                    style = TapTapType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = own.onAccent,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TapTapProgressBar(
                    progress = milestoneProgress,
                    height = 8.dp,
                    trackColor = own.accentStrong,
                    fillColor = own.onAccent,
                )
                Text(text = nextBadgeText, style = TapTapType.label, color = own.onAccent)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                FocusStat(moneySavedText, sectionLabels.moneySaved, Modifier.weight(1f))
                FocusStat(urgesTodayText, sectionLabels.urgesToday, Modifier.weight(1f))
            }

            // The single action.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(own.onAccent, TapTapShapes.md)
                    .clickable(role = Role.Button, onClick = onBeatUrge),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                ) {
                    Icon(
                        imageVector = TapTapIcons.Plus,
                        contentDescription = null,
                        tint = own.accentStrong,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = sectionLabels.beatUrge,
                        style = TapTapType.title,
                        color = own.accentStrong,
                    )
                }
            }

            if (!counter.reason.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.quoted, counter.reason),
                    style = TapTapType.label.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = own.onAccent,
                )
            }
        }
    }
}

@Composable
private fun FocusStat(value: String, label: String, modifier: Modifier = Modifier) {
    val own = TapTapTheme.colors
    Column(
        modifier = modifier
            .border(1.dp, own.accentStrong, TapTapShapes.md)
            .padding(horizontal = Spacing.space3, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = TapTapType.statValue, color = own.onAccent)
        Text(text = label, style = TapTapType.caption, color = own.onAccent)
    }
}

/** The card's fixed copy, passed in so TT-027 can supply it from `strings.xml`. */
data class FocusCardLabels(
    val daysFree: String,
    val moneySaved: String,
    val urgesToday: String,
    val beatUrge: String,
    val open: String,
)
