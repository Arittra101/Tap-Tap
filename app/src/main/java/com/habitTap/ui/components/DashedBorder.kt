package com.habitTap.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The 2dp dashed edge the design uses for the "pick a focus" card and the "New counter"
 * card. Compose has no dashed-border modifier, so it is drawn.
 */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    width: Dp = 2.dp,
    dash: Dp = 6.dp,
    gap: Dp = 5.dp,
): Modifier = drawBehind {
    val outline: Outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dash.toPx(), gap.toPx()),
                0f,
            ),
        ),
    )
}
