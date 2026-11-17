package com.habitTap.ui.journey

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.habitTap.R
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.ButtonVariant
import com.habitTap.ui.components.TapTapButton
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import kotlin.math.max

/**
 * The journey's character and what it is saying, on top of a screen.
 *
 * While a step is showing, the ringed area is the only part of the screen that answers a
 * touch, and the back button does nothing. "Stop tutorial" in the bubble is the one way
 * out, and it is on every step.
 *
 * Draws nothing until [anchor] has been laid out, so a step whose target is not on screen
 * — the counter card that does not exist yet — stays quiet instead of pointing at a corner.
 */
@Composable
fun JourneyCoach(
    text: String,
    anchor: JourneyAnchor,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Shown as a Next button in the card when the step is one the user finishes by saying
     * so rather than by doing something visible — typing a title, where only they know
     * they are done. Null on every other step, which advance by themselves.
     */
    onNext: (() -> Unit)? = null,
) {
    val c = TapTapTheme.colors
    val density = LocalDensity.current

    // Back does nothing while a step is showing.
    //
    // Registered here, before the step's target has been measured, so that back is
    // blocked for as long as the step is up — not only while the ring happens to be on
    // screen. It is also registered after the screens' own back handlers, and the
    // dispatcher calls the most recent one, so this takes precedence over them.
    BackHandler(enabled = true) { /* "Stop tutorial" is the way out. */ }

    // The overlay's own position, so the anchor's root coordinates can be read in the
    // overlay's space. The two differ by whatever padding sits above the overlay.
    var origin by remember { mutableStateOf(Offset.Zero) }
    var overlaySize by remember { mutableStateOf(Size.Zero) }
    var bubbleHeight by remember { mutableIntStateOf(0) }

    val target = anchor.bounds?.translate(-origin.x, -origin.y)

    // The Next button's zoom. A separate, quicker beat from the ring's: the ring says
    // "look here", the button says "press me".
    val nudge by rememberInfiniteTransition(label = "journey-next").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "journey-next-zoom",
    )

    // A slow breath on the ring. Enough to catch the eye on a screen the user is reading,
    // not enough to be the loudest thing on it.
    val pulse by rememberInfiniteTransition(label = "journey-ring").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "journey-ring-pulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin = it.positionInRoot()
                overlaySize = Size(it.size.width.toFloat(), it.size.height.toFloat())
            },
    ) {
        if (target == null || overlaySize == Size.Zero) return@Box

        val ringPad = with(density) { 6.dp.toPx() }
        val ring = Rect(
            left = target.left - ringPad,
            top = target.top - ringPad,
            right = target.right + ringPad,
            bottom = target.bottom + ringPad,
        )

        // A target that has been scrolled off the screen is not something the user can
        // be asked to tap, so the character waits rather than pointing past the edge.
        if (ring.bottom <= 0f || ring.top >= overlaySize.height) return@Box

        // Only the ringed area answers a touch while the step is showing.
        //
        // Four strips around the hole rather than one sheet with a hole drawn in it:
        // Compose hit-tests siblings topmost-first and stops at the one it hits, so a
        // full-screen blocker would swallow the taps meant for the target as well, however
        // it was painted. With strips, there is simply nothing above the target to catch
        // them.
        TouchBlocker(
            Modifier
                .offset { IntOffset(0, 0) }
                .size(overlaySize.width.toIntDp(density), ring.top.toIntDp(density)),
        )
        TouchBlocker(
            Modifier
                .offset { IntOffset(0, ring.bottom.toInt()) }
                .size(
                    overlaySize.width.toIntDp(density),
                    (overlaySize.height - ring.bottom).toIntDp(density),
                ),
        )
        TouchBlocker(
            Modifier
                .offset { IntOffset(0, ring.top.toInt()) }
                .size(ring.left.toIntDp(density), ring.height.toIntDp(density)),
        )
        TouchBlocker(
            Modifier
                .offset { IntOffset(ring.right.toInt(), ring.top.toInt()) }
                .size(
                    (overlaySize.width - ring.right).toIntDp(density),
                    ring.height.toIntDp(density),
                ),
        )

        // The dim, with the target punched out of it. The offscreen layer is what makes
        // BlendMode.Clear clear this layer rather than everything painted below it.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .clearAndSetSemantics { },
        ) {
            val radius = CornerRadius(with(density) { 20.dp.toPx() })
            drawRect(c.dim)
            drawRoundRect(
                color = Color.Black,
                topLeft = ring.topLeft,
                size = ring.size,
                cornerRadius = radius,
                blendMode = BlendMode.Clear,
            )
            drawRoundRect(
                color = c.accent.copy(alpha = 0.55f + 0.45f * pulse),
                topLeft = ring.topLeft,
                size = ring.size,
                cornerRadius = radius,
                style = Stroke(width = with(density) { (2 + 1.5f * pulse).dp.toPx() }),
            )
        }

        // --- Where the character goes -------------------------------------------------
        // Below the target when the target is in the top half of the screen, above it
        // otherwise: whichever side has the room, so the bubble never covers the very
        // thing it is pointing at.
        val gap = with(density) { Spacing.space3.toPx() }
        val margin = with(density) { Spacing.space4.toPx() }
        val blockWidth = with(density) { BubbleWidth.toPx() }
        val below = ring.top < overlaySize.height / 2f

        val blockX = (ring.center.x - blockWidth / 2f)
            .coerceIn(margin, max(margin, overlaySize.width - blockWidth - margin))
        // Clamped into the screen: a target close to an edge would otherwise push the
        // bubble half out of view.
        val blockY = (if (below) ring.bottom + gap else ring.top - gap - bubbleHeight)
            .coerceIn(margin, max(margin, overlaySize.height - bubbleHeight - margin))

        // The tail sits under the target's centre where it can, and stays on the bubble.
        val tailInset = with(density) {
            (ring.center.x - blockX - TailWidth.toPx() / 2f)
                .coerceIn(Spacing.space4.toPx(), blockWidth - Spacing.space4.toPx() - TailWidth.toPx())
                .toDp()
        }

        Column(
            modifier = Modifier
                .width(BubbleWidth)
                .offset { IntOffset(blockX.toInt(), blockY.toInt()) }
                .onSizeChanged { bubbleHeight = it.height }
                // Measured on the first pass, placed on the second: without this the
                // bubble would flash at the top of the screen for one frame.
                .alpha(if (bubbleHeight == 0) 0f else 1f),
        ) {
            if (below) Tail(pointsDown = false, inset = tailInset)
            Row(
                modifier = Modifier
                    .swallowTouches()
                    .clip(TapTapShapes.lg)
                    .background(c.surfaceRaised)
                    .border(1.dp, c.line, TapTapShapes.lg)
                    .padding(Spacing.space3),
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Mascot()
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                    Text(
                        text = text,
                        style = TapTapType.body,
                        color = c.ink,
                        // Announced when the step changes, so a screen reader user is told
                        // what to do without having to hunt for the bubble.
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    if (onNext != null) {
                        TapTapButton(
                            text = stringResource(R.string.tutorial_next),
                            onClick = onNext,
                            variant = ButtonVariant.Primary,
                            minHeight = 40.dp,
                            // Breathes, so a button that appears mid-sentence is noticed.
                            modifier = Modifier.graphicsLayer {
                                val scale = 1f + 0.06f * nudge
                                scaleX = scale
                                scaleY = scale
                            },
                        )
                    }
                    Text(
                        text = stringResource(R.string.journey_stop),
                        style = TapTapType.label,
                        color = c.inkMuted,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onStop)
                            .padding(horizontal = Spacing.space2, vertical = Spacing.space2),
                    )
                }
            }
            if (!below) Tail(pointsDown = true, inset = tailInset)
        }
    }
}

/**
 * A strip of screen that answers nothing.
 *
 * Every pointer change is consumed, not just taps, so a drag or a fling over a blocked
 * area does not scroll the screen underneath either.
 */
@Composable
private fun TouchBlocker(modifier: Modifier) {
    Box(modifier.swallowTouches().clearAndSetSemantics { })
}

/**
 * Eats pointer input.
 *
 * On the main pass, so a clickable *inside* this node — the bubble's "Stop tutorial" —
 * is still dispatched first and still works.
 */
private fun Modifier.swallowTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent().changes.forEach { it.consume() }
        }
    }
}

/** Pixels from a [Rect] back to the dp a [Modifier.size] wants, never negative. */
private fun Float.toIntDp(density: Density): Dp = with(density) { coerceAtLeast(0f).toDp() }

private val BubbleWidth = 292.dp
private val TailWidth = 18.dp
private val TailHeight = 10.dp

/** The little triangle that aims the bubble at what the step is about. */
@Composable
private fun Tail(pointsDown: Boolean, inset: androidx.compose.ui.unit.Dp) {
    val c = TapTapTheme.colors
    Canvas(
        modifier = Modifier
            .padding(start = inset)
            .size(TailWidth, TailHeight)
            .clearAndSetSemantics { },
    ) {
        val path = Path().apply {
            if (pointsDown) {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            } else {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            }
            close()
        }
        drawPath(path, c.surfaceRaised)
    }
}

/**
 * The guide: a rounded body in the theme's accent with two eyes and a smile.
 *
 * Drawn rather than drawn-on-disk, so it costs nothing in the APK and recolours itself
 * with the theme — the character is saffron by default and teal for someone who picked
 * teal, without four sets of artwork.
 */
@Composable
private fun Mascot(modifier: Modifier = Modifier) {
    val c = TapTapTheme.colors
    val bob by rememberInfiniteTransition(label = "journey-mascot").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "journey-mascot-bob",
    )

    Canvas(modifier = modifier.size(52.dp).clearAndSetSemantics { }) {
        val lift = size.height * 0.04f * (bob - 0.5f)
        val body = Rect(
            left = size.width * 0.08f,
            top = size.height * 0.14f + lift,
            right = size.width * 0.92f,
            bottom = size.height * 0.92f + lift,
        )
        // Feet, so it sits on something rather than floating.
        drawRoundRect(
            color = c.accentSoft,
            topLeft = Offset(body.left, body.bottom - size.height * 0.08f),
            size = Size(body.width, size.height * 0.12f),
            cornerRadius = CornerRadius(size.width * 0.06f),
        )
        drawRoundRect(
            color = c.accent,
            topLeft = body.topLeft,
            size = body.size,
            cornerRadius = CornerRadius(size.width * 0.34f),
        )
        // The antenna: a counter's tick, so the guide looks like it belongs to the app.
        drawLine(
            color = c.accentStrong,
            start = Offset(body.center.x, body.top),
            end = Offset(body.center.x, body.top - size.height * 0.1f),
            strokeWidth = size.width * 0.05f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = c.accentStrong,
            radius = size.width * 0.06f,
            center = Offset(body.center.x, body.top - size.height * 0.13f),
        )
        val eyeY = body.top + body.height * 0.38f
        val eyeR = size.width * 0.075f
        drawCircle(c.onAccent, eyeR, Offset(body.center.x - body.width * 0.2f, eyeY))
        drawCircle(c.onAccent, eyeR, Offset(body.center.x + body.width * 0.2f, eyeY))
        // The smile, as an arc struck through a box below the eyes.
        val mouth = Rect(
            left = body.center.x - body.width * 0.22f,
            top = eyeY + body.height * 0.06f,
            right = body.center.x + body.width * 0.22f,
            bottom = eyeY + body.height * 0.34f,
        )
        drawArc(
            color = c.onAccent,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = mouth.topLeft,
            size = mouth.size,
            style = Stroke(width = size.width * 0.05f, cap = StrokeCap.Round),
        )
    }
}

@Preview(name = "Journey · coach bubble")
@Composable
private fun JourneyCoachPreview() {
    TapTapTheme(ThemeColor.SAFFRON, ThemeMode.LIGHT) {
        Box(Modifier.size(360.dp, 260.dp).background(TapTapTheme.colors.bg)) {
            val anchor = rememberJourneyAnchor()
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.space4)
                    .size(160.dp, 56.dp)
                    .clip(TapTapShapes.lg)
                    .background(TapTapTheme.colors.ink)
                    .journeyAnchor(anchor),
            )
            JourneyCoach(
                text = "Let's make your first counter. Tap here.",
                anchor = anchor,
                onStop = {},
            )
        }
    }
}
