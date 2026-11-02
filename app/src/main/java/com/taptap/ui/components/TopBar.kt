package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taptap.R
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * The Home top bar: the app mark, "Tap Tap", and one action (FR-1.1).
 *
 * The bar is the same colour as the page and carries no shadow — the 1dp `line` divider
 * is what separates it. That is a "Don't" in the design system's TopBar page.
 */
@Composable
fun HomeTopBar(
    onSettings: () -> Unit,
    settingsLabel: String,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(c.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.space16)
                .padding(horizontal = Spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppMark()
                Text(
                    text = stringResource(R.string.app_wordmark),
                    style = TapTapType.titleLg,
                    color = c.ink,
                    modifier = Modifier.semantics { heading() },
                )
            }
            TapTapIconButton(
                icon = TapTapIcons.Settings,
                contentDescription = settingsLabel,
                onClick = onSettings,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = c.line)
    }
}

/** The `tt-mark`: a 40dp `radius-md` square in `accent` with the plus glyph. */
@Composable
fun AppMark(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val c = TapTapTheme.colors
    Box(
        modifier = modifier.size(size).background(c.accent, TapTapShapes.md),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TapTapIcons.Plus,
            // Decorative: the "Tap Tap" wordmark beside it carries the meaning.
            contentDescription = null,
            tint = c.onAccent,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

/**
 * The inner-screen top bar: back, a centred title with an optional subtitle, and at most
 * one action on the right.
 *
 * When there is no action, an empty 48dp slot takes its place so the title stays centred
 * — the design system calls for this explicitly.
 */
@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    backLabel: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    titleClickLabel: String? = null,
) {
    val c = TapTapTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(c.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Spacing.space16)
                .padding(horizontal = Spacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TapTapIconButton(
                icon = TapTapIcons.Back,
                contentDescription = backLabel,
                onClick = onBack,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.space2)
                    .then(
                        // The quit screen needs a route to Edit, but FR-4.1 gives its one
                        // right-hand action to the pin. Making the title open Edit adds no
                        // visual element and keeps that rule.
                        if (onTitleClick != null) {
                            Modifier
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = titleClickLabel,
                                    onClick = onTitleClick,
                                )
                        } else {
                            Modifier
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = TapTapType.screenTitle,
                    color = c.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = TapTapType.caption,
                        color = c.inkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (action != null) {
                action()
            } else {
                // Keeps the title optically centred.
                Box(Modifier.size(48.dp))
            }
        }
        HorizontalDivider(thickness = 1.dp, color = c.line)
    }
}
