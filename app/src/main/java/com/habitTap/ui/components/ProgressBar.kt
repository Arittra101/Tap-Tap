package com.habitTap.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme

/**
 * A flat progress bar. The design uses borders and fills, never a Material indicator.
 *
 * Marked decorative: the number beside every bar in this app already carries the value,
 * so announcing a percentage as well would just be noise.
 */
@Composable
fun TapTapProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 6.dp,
    trackColor: Color? = null,
    fillColor: Color? = null,
) {
    val c = TapTapTheme.colors
    val value by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(trackColor ?: c.surfaceSunken, TapTapShapes.pill)
            .clearAndSetSemantics { },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .layout { measurable, constraints ->
                    val w = (constraints.maxWidth * value).toInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .background(fillColor ?: c.accent, TapTapShapes.pill),
        )
    }
}
