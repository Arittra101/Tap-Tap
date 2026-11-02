package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * A stand-in screen.
 *
 * TT-019 lands the navigation graph with these so back navigation, ViewModel scoping and
 * rotation can be tested before any screen exists (tech spec build order, step 3). Each
 * Epic E ticket replaces exactly one of them.
 */
@Composable
fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = TapTapType.titleLg, color = c.ink)
        if (detail != null) Text(detail, style = TapTapType.caption, color = c.inkMuted)
        actions.forEach { (label, onClick) ->
            TextButton(onClick = onClick) { Text(label, style = TapTapType.label, color = c.accentStrong) }
        }
    }
}
