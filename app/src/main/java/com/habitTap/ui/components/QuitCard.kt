package com.habitTap.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitTap.model.Counter
import com.habitTap.util.localNumber
import com.habitTap.ui.theme.CounterTheme
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * One quit counter in Home's vertical "Quit tracker" list (FR-1.8).
 *
 * [sideText] is money saved when the user set a daily spend, and urges beaten today when
 * they did not — the design swaps the two rather than showing a meaningless zero.
 *
 * The progress bar measures days toward the **next badge**, not toward the goal.
 */
@Composable
fun QuitCard(
    counter: Counter,
    daysFree: Int,
    daysFreeLabel: String,
    sideText: String,
    nextBadgeText: String,
    milestoneProgress: Float,
    onOpen: () -> Unit,
    onMakeFocus: () -> Unit,
    onTogglePin: () -> Unit,
    makeFocusLabel: String,
    makeFocusText: String,
    pinLabel: String,
    openLabel: String,
    modifier: Modifier = Modifier,
) {
    val app = TapTapTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(TapTapShapes.lg)
            .clickable(role = Role.Button, onClickLabel = openLabel, onClick = onOpen)
            .background(app.surface)
            .border(
                width = 1.dp,
                color = if (counter.isPinned) app.lineStrong else app.line,
                shape = TapTapShapes.lg,
            )
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CounterTheme(counter.color) {
            val own = TapTapTheme.colors

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(own.accent, TapTapShapes.pill),
                )
                Text(
                    text = counter.name,
                    style = TapTapType.title,
                    color = app.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.space2),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Text(
                        text = localNumber(daysFree),
                        style = TapTapType.stat.copy(fontSize = 36.sp, lineHeight = 40.sp),
                        color = own.accentStrong,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = daysFreeLabel,
                        style = TapTapType.label,
                        color = app.inkMuted,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(
                    text = sideText,
                    style = TapTapType.label.copy(fontWeight = FontWeight.SemiBold),
                    color = app.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            TapTapProgressBar(progress = milestoneProgress, fillColor = own.accent)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                Text(
                    text = nextBadgeText,
                    style = TapTapType.caption,
                    color = app.inkMuted,
                    modifier = Modifier.weight(1f),
                )
                // FR-1.8 needs a make-focus control here. A bare two-circles icon reads
                // as a radio button, so it carries its word instead.
                Row(
                    modifier = Modifier
                        .clip(TapTapShapes.pill)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = makeFocusLabel,
                            onClick = onMakeFocus,
                        )
                        .background(own.accentSoft)
                        .padding(horizontal = Spacing.space3, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = TapTapIcons.Focus,
                        contentDescription = null,
                        tint = own.onAccentSoft,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = makeFocusText,
                        style = TapTapType.caption.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        ),
                        color = own.onAccentSoft,
                    )
                }
            }
        }
    }
}
