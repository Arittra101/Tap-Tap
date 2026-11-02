package com.taptap.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taptap.util.localNumber
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/** What the dial is doing right now. Each state says itself in a word (NFR-6). */
enum class DialState { Counting, Reverse, Locked, TargetReached }

/**
 * The counter dial (FR-3.3).
 *
 * The *whole circle* is one button, which is the point: the app is meant to be usable
 * without looking at it. Everything inside is marked decorative so TalkBack announces one
 * target, not five, and the count is a live region so each change is read out.
 */
@Composable
fun CounterDial(
    count: Long,
    state: DialState,
    pillText: String,
    hintText: String,
    tapLabel: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    target: Long? = null,
    ofText: String? = null,
    diameter: androidx.compose.ui.unit.Dp = 310.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 18.dp,
) {
    val c = TapTapTheme.colors
    val enabled = state == DialState.Counting || state == DialState.Reverse

    /*
     * NFR-7, the awkward case. `count-xl` is 104sp; at a 200% system font scale that is
     * 208sp, which fits inside no phone — the ring squashed to an ellipse and the tap
     * hint was clipped. The number is already display-sized, so its scaling is capped
     * here while every label around it keeps scaling normally.
     */
    val fontScale = LocalDensity.current.fontScale
    val countStyle = if (fontScale > COUNT_SCALE_CAP) {
        TapTapType.countXl.copy(
            fontSize = TapTapType.countXl.fontSize * (COUNT_SCALE_CAP / fontScale),
            lineHeight = TapTapType.countXl.lineHeight * (COUNT_SCALE_CAP / fontScale),
        )
    } else {
        TapTapType.countXl
    }

    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    // The design's motion rule: 96-98% for 90ms, no bounce.
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "dialPress",
    )

    val fraction = when {
        target == null || target <= 0L -> 0f
        else -> (count.toFloat() / target).coerceIn(0f, 1f)
    }
    val sweep by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 180),
        label = "dialProgress",
    )

    val ringColor = when (state) {
        DialState.TargetReached -> c.success
        DialState.Locked -> c.lineStrong
        else -> c.accent
    }
    val faceColor = if (state == DialState.Locked) c.surfaceSunken else c.surface
    val countColor = when (state) {
        DialState.Locked -> c.inkMuted
        DialState.TargetReached -> c.accentStrong
        else -> c.ink
    }
    val hintColor = if (enabled) c.accentStrong else c.inkMuted
    val pillTone = when (state) {
        DialState.Locked -> PillTone.Muted
        DialState.TargetReached -> PillTone.Success
        else -> PillTone.Accent
    }
    val pillIcon = when (state) {
        DialState.Locked -> TapTapIcons.Lock
        DialState.TargetReached -> TapTapIcons.Check
        DialState.Reverse -> TapTapIcons.Reverse
        DialState.Counting -> null
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
    // The dial never exceeds the space it is given, so a narrow screen shrinks it
    // rather than clipping it.
    val dialSize = minOf(diameter, maxWidth, maxHeight)

    Box(
        modifier = Modifier
            .size(dialSize)
            .scale(pressScale)
            .selectable(
                selected = false,
                enabled = enabled,
                onClick = onTap,
                role = Role.Button,
                indication = ripple(bounded = false, radius = dialSize / 2),
                interactionSource = interaction,
            )
            // One target, one label — whatever the state (NFR-4).
            .semantics(mergeDescendants = true) { contentDescription = tapLabel },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(dialSize)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Face.
            drawCircle(color = faceColor, radius = (size.minDimension - stroke) / 2f)
            // Track.
            drawArc(
                color = c.surfaceSunken,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            // Progress, from 12 o'clock.
            if (sweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            } else if (target == null) {
                // No target: a quiet full ring rather than an empty one, so the dial does
                // not look broken (FR-1.6's "no target" case).
                drawArc(
                    color = c.accentSoft,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = Spacing.space6),
        ) {
            StatusPill(
                text = pillText,
                tone = pillTone,
                icon = pillIcon,
                modifier = Modifier.clearAndSetSemantics { },
            )
            Text(
                text = localNumber(count),
                style = countStyle,
                color = countColor,
                textAlign = TextAlign.Center,
                // Announce each change without re-reading the whole dial.
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (ofText != null) {
                Text(
                    text = ofText,
                    style = TapTapType.body,
                    color = c.inkMuted,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
            Text(
                text = hintText,
                style = TapTapType.title.copy(fontFamily = TapTapType.screenTitle.fontFamily),
                color = hintColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = Spacing.space2)
                    .clearAndSetSemantics { },
            )
        }
    }
    }
}

/** How far the dial's number is allowed to grow with the system font setting. */
private const val COUNT_SCALE_CAP = 1.3f

/** The quit screen's days ring: same geometry, no tap target (FR-4.2). */
@Composable
fun DaysRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 270.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 16.dp,
    ringColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val c = TapTapTheme.colors
    val sweep by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 180),
        label = "daysRing",
    )
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // Same rule as the counter dial: never larger than the space available.
        val ringSize = minOf(diameter, maxWidth, maxHeight)
        Box(modifier = Modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(ringSize)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawCircle(color = c.surface, radius = (size.minDimension - stroke) / 2f)
            drawArc(
                color = c.surfaceSunken,
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke),
            )
            if (sweep > 0f) {
                drawArc(
                    color = ringColor ?: c.accent,
                    startAngle = -90f, sweepAngle = 360f * sweep, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Box(
            modifier = Modifier.padding(horizontal = Spacing.space6),
            contentAlignment = Alignment.Center,
        ) { content() }
        }
    }
}
