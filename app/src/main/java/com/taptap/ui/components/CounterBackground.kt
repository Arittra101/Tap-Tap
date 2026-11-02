package com.taptap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.taptap.model.Background
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme

/**
 * The three default counter backgrounds (FR-5.2), plus the scrim that keeps the number
 * readable over them (FR-3.9).
 *
 * These are **drawn**, not shipped as images. FR-7.6 says "bundled images" and NFR-9
 * allows "WebP or vector", but the design canvas draws each one as a few shapes filled
 * with the counter's own `accent` over an `accent-soft` ground — and the Edit screen
 * promises "Default images take the counter's color". A bitmap could not do that. Drawing
 * them also costs nothing against the 15 MB budget.
 *
 * The geometry below is taken from the canvas's 390x844 viewBox and scaled to fit.
 */
@Composable
fun CounterBackground(
    background: Background,
    modifier: Modifier = Modifier,
    withScrim: Boolean = true,
) {
    if (background == Background.NONE) return
    val c = TapTapTheme.colors

    Box(modifier = modifier.fillMaxSize().clearAndSetSemantics { }) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(c.accentSoft)
            // The screen already has the reference aspect ratio, so no cropping is needed.
            drawArt(background, c.accent, Frame(size.width, size.height))
        }
        if (withScrim) {
            Canvas(Modifier.fillMaxSize()) { drawRect(c.scrim) }
        }
    }
}

/**
 * A small tile of a background, for Edit's picker.
 *
 * Two things the tile has to get right that the full-screen version does not:
 *
 * 1. It is **clipped to [shape]**. A bare `Canvas` draws its `drawRect` ground over the
 *    rounded corners of whatever card it sits in, which reads as the artwork being sliced
 *    off at the radius.
 * 2. The artwork is **centre-cropped**, not squashed. The shapes were authored against a
 *    390x844 portrait screen; mapping that onto a square tile directly would compress
 *    everything vertically and show a band of waves instead of a recognisable preview. So
 *    the reference frame is scaled to *cover* the tile and centred, exactly as
 *    `ContentScale.Crop` would do for a bitmap.
 */
@Composable
fun BackgroundTile(
    background: Background,
    modifier: Modifier = Modifier,
    shape: Shape = TapTapShapes.sm,
) {
    val c = TapTapTheme.colors
    Canvas(
        modifier
            .clip(shape)
            .clearAndSetSemantics { },
    ) {
        if (background == Background.NONE) {
            drawRect(c.surfaceSunken)
            return@Canvas
        }
        drawRect(c.accentSoft)

        // Cover: scale by the larger ratio so neither axis is left short, then centre.
        val scale = maxOf(this.size.width / REF_W, this.size.height / REF_H)
        val artW = REF_W * scale
        val artH = REF_H * scale
        withTransform({
            translate(
                left = (this.size.width - artW) / 2f,
                top = (this.size.height - artH) / 2f,
            )
        }) {
            drawArt(background, c.accent, Frame(artW, artH))
        }
    }
}

// The canvas authored these against 390 x 844.
private const val REF_W = 390f
private const val REF_H = 844f

/**
 * The box the 390x844 reference coordinates are mapped onto. For the full screen it is the
 * node itself; for a cropped tile it is the larger, centred box that covers the node.
 */
private class Frame(val w: Float, val h: Float)

private fun Frame.sx(x: Float) = x / REF_W * w
private fun Frame.sy(y: Float) = y / REF_H * h

/** Scales a radius by the smaller axis, so circles stay circles. */
private fun Frame.sr(r: Float) = r / REF_W * minOf(w, h * REF_W / REF_H)

private fun DrawScope.drawArt(background: Background, accent: Color, f: Frame) {
    when (background) {
        Background.WAVES -> drawWaves(accent, f)
        Background.ORBS -> drawOrbs(accent, f)
        Background.HILLS -> drawHills(accent, f)
        Background.NONE -> Unit
    }
}

private fun DrawScope.drawWaves(accent: Color, f: Frame) = with(f) {
    fun wave(startY: Float, c1y: Float, c2y: Float, midY: Float, endY: Float, alpha: Float) {
        val path = Path().apply {
            moveTo(sx(0f), sy(startY))
            cubicTo(sx(90f), sy(c1y), sx(180f), sy(c2y), sx(270f), sy(midY))
            cubicTo(sx(330f), sy(midY - 30f), sx(390f), sy(endY), sx(390f), sy(endY))
            lineTo(sx(390f), sy(844f))
            lineTo(sx(0f), sy(844f))
            close()
        }
        drawPath(path, accent, alpha = alpha, style = Fill)
    }
    wave(520f, 470f, 580f, 530f, 470f, 0.22f)
    wave(610f, 560f, 660f, 610f, 580f, 0.32f)
    wave(700f, 660f, 740f, 700f, 680f, 0.45f)
    drawCircle(accent, radius = sr(60f), center = Offset(sx(300f), sy(150f)), alpha = 0.18f)
}

private fun DrawScope.drawOrbs(accent: Color, f: Frame) = with(f) {
    drawCircle(accent, sr(90f), Offset(sx(60f), sy(140f)), alpha = 0.18f)
    drawCircle(accent, sr(130f), Offset(sx(340f), sy(360f)), alpha = 0.14f)
    drawCircle(accent, sr(160f), Offset(sx(110f), sy(700f)), alpha = 0.20f)
    drawCircle(accent, sr(70f), Offset(sx(330f), sy(790f)), alpha = 0.30f)
}

private fun DrawScope.drawHills(accent: Color, f: Frame) = with(f) {
    fun ridge(points: List<Pair<Float, Float>>, alpha: Float) {
        val path = Path().apply {
            moveTo(sx(points.first().first), sy(points.first().second))
            points.drop(1).forEach { (x, y) -> lineTo(sx(x), sy(y)) }
            lineTo(sx(390f), sy(844f))
            lineTo(sx(0f), sy(844f))
            close()
        }
        drawPath(path, accent, alpha = alpha, style = Fill)
    }
    drawCircle(accent, sr(48f), Offset(sx(280f), sy(230f)), alpha = 0.35f)
    ridge(listOf(0f to 560f, 120f to 430f, 230f to 540f, 320f to 460f, 390f to 520f), 0.20f)
    ridge(listOf(0f to 660f, 90f to 580f, 200f to 650f, 300f to 570f, 390f to 640f), 0.32f)
    ridge(listOf(0f to 760f, 140f to 690f, 260f to 750f, 390f to 700f), 0.45f)
}
