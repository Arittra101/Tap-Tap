package com.habitTap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitTap.R
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapShapes
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType

/**
 * A 48dp input on `surface-sunken` with a `line-strong` border; focus adds an
 * `accent-strong` ring.
 *
 * Required fields get a `danger` asterisk; optional ones say "Optional" in `ink-muted`.
 * The design makes that distinction visible rather than relying on the user discovering
 * it when a button stays disabled.
 */
@Composable
fun TapTapTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    help: String? = null,
    required: Boolean = false,
    optionalLabel: String? = null,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
) {
    val c = TapTapTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = TapTapType.label, color = c.ink)
            if (required) {
                Text(
                    text = stringResource(R.string.required_marker),
                    style = TapTapType.label,
                    color = c.danger,
                )
            } else if (optionalLabel != null) {
                Text(text = optionalLabel, style = TapTapType.caption, color = c.inkMuted)
            }
        }

        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = c.accentStrong,
                backgroundColor = c.accentSoft,
            ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TapTapType.body.copy(color = c.ink),
                cursorBrush = SolidColor(c.accentStrong),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .background(c.surfaceSunken, TapTapShapes.md)
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) c.accentStrong else c.lineStrong,
                        shape = TapTapShapes.md,
                    )
                    .padding(horizontal = Spacing.space3, vertical = 12.dp)
                    .semantics { },
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (prefix != null) {
                            Text(
                                text = prefix,
                                style = TapTapType.body,
                                color = c.inkMuted,
                                modifier = Modifier.padding(end = Spacing.space2),
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            if (value.isEmpty() && placeholder != null) {
                                Text(text = placeholder, style = TapTapType.body, color = c.inkMuted)
                            }
                            inner()
                        }
                    }
                },
            )
        }

        if (help != null) {
            Text(text = help, style = TapTapType.caption, color = c.inkMuted)
        }
    }
}
