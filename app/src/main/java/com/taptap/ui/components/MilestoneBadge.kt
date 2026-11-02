package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taptap.util.localNumber
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** Where a milestone stands for this counter. */
enum class BadgeState { Earned, Next, Locked }

/**
 * One 56dp milestone badge with its label beneath (FR-4.6).
 *
 * [stateLabel] is the word for the state — "earned", "next", "locked" — and it goes into
 * the content description, so the three states are distinguishable without seeing the
 * fill (NFR-6).
 */
@Composable
fun MilestoneBadge(
    days: Int,
    label: String,
    state: BadgeState,
    stateLabel: String,
    badgeWord: String,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .width(60.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$days $badgeWord, $stateLabel"
                role = Role.Image
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    when (state) {
                        BadgeState.Earned -> c.accent
                        BadgeState.Next -> c.surface
                        BadgeState.Locked -> c.surfaceSunken
                    },
                    TapTapShapes.pill,
                )
                .then(
                    if (state == BadgeState.Next) {
                        Modifier.dashedBorder(c.accentStrong, TapTapShapes.pill)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state == BadgeState.Earned) {
                Icon(
                    imageVector = TapTapIcons.Check,
                    contentDescription = null,
                    tint = c.onAccent,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = localNumber(days),
                    style = TapTapType.label.copy(fontFamily = TapTapType.statValue.fontFamily),
                    color = if (state == BadgeState.Next) c.accentStrong else c.inkMuted,
                )
            }
        }
        Text(
            text = label,
            style = TapTapType.caption.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
            color = if (state == BadgeState.Locked) c.inkMuted else c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

/** The ten badges in a horizontal scroll, bleeding to the screen edges. */
@Composable
fun MilestoneRow(
    badges: List<MilestoneBadgeData>,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = Spacing.space4,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        badges.forEach { badge ->
            MilestoneBadge(
                days = badge.days,
                label = badge.label,
                state = badge.state,
                stateLabel = badge.stateLabel,
                badgeWord = badge.badgeWord,
            )
        }
    }
}

/** One badge's data, assembled by the screen so this file needs no string resources. */
data class MilestoneBadgeData(
    val days: Int,
    val label: String,
    val state: BadgeState,
    val stateLabel: String,
    val badgeWord: String,
)
