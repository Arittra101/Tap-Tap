package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taptap.util.localNumber
import com.taptap.ui.theme.MinTouchTarget
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A number between a minus and a plus, with an optional help line.
 *
 * Long-press repeats, which the design system suggests and which matters here: setting a
 * target of 50 one tap at a time would be miserable.
 */
@Composable
fun Stepper(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    decreaseLabel: String,
    increaseLabel: String,
    modifier: Modifier = Modifier,
    help: String? = null,
    min: Long = 0,
    max: Long = Long.MAX_VALUE,
    step: Long = 1,
) {
    val c = TapTapTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = label, style = TapTapType.label, color = c.ink)
            if (help != null) {
                Text(text = help, style = TapTapType.caption, color = c.inkMuted)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            StepperButton(
                icon = TapTapIcons.Minus,
                label = decreaseLabel,
                enabled = value > min,
                onStep = { onValueChange((value - step).coerceAtLeast(min)) },
            )
            Text(
                text = localNumber(value),
                style = TapTapType.statValue,
                color = c.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 48.dp),
            )
            StepperButton(
                icon = TapTapIcons.Plus,
                label = increaseLabel,
                enabled = value < max,
                onStep = { onValueChange((value + step).coerceAtMost(max)) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onStep: () -> Unit,
) {
    val c = TapTapTheme.colors
    val currentStep by rememberUpdatedState(onStep)

    Box(
        modifier = Modifier
            // The visual button is 44dp, but NFR-4 wants 48dp of touchable area.
            .sizeIn(minWidth = MinTouchTarget, minHeight = MinTouchTarget)
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        // One step on press, then hold to repeat, accelerating to a floor
                        // of 60ms. Release cancels.
                        coroutineScope {
                            currentStep()
                            val repeat = launch {
                                delay(FIRST_REPEAT_DELAY_MS)
                                var wait = REPEAT_START_MS
                                while (true) {
                                    currentStep()
                                    delay(wait)
                                    wait = (wait * 4 / 5).coerceAtLeast(REPEAT_FLOOR_MS)
                                }
                            }
                            tryAwaitRelease()
                            repeat.cancel()
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(c.surfaceSunken, TapTapShapes.md)
                .border(1.dp, c.lineStrong, TapTapShapes.md),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) c.ink else c.inkMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private const val FIRST_REPEAT_DELAY_MS = 400L
private const val REPEAT_START_MS = 200L
private const val REPEAT_FLOOR_MS = 60L
