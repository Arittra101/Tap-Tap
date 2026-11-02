package com.taptap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode

/**
 * Verification for TT-002/003/004: one card drawn in all eight themes, so the tokens,
 * the type scale and the radii can be eyeballed together.
 */
@Composable
private fun TokenCard(label: String) {
    val c = TapTapTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Text(label, style = TapTapType.titleLg, color = c.ink)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surface, TapTapShapes.lg)
                .border(1.dp, c.line, TapTapShapes.lg)
                .padding(Spacing.space4),
            verticalArrangement = Arrangement.spacedBy(Spacing.space1),
        ) {
            Text("32", style = TapTapType.stat, color = c.accentStrong)
            Text("Counter name", style = TapTapType.title, color = c.ink)
            Text("Helper line in ink-muted", style = TapTapType.caption, color = c.inkMuted)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Swatch(c.accent, c.onAccent, "A")
                Swatch(c.accentSoft, c.onAccentSoft, "S")
                Swatch(c.danger, c.onDanger, "D")
                Swatch(c.success, c.onSuccess, "T")
                Swatch(c.surfaceSunken, c.ink, "K")
            }
        }
    }
}

@Composable
private fun Swatch(fill: androidx.compose.ui.graphics.Color, on: androidx.compose.ui.graphics.Color, letter: String) {
    Box(
        modifier = Modifier.size(40.dp).background(fill, TapTapShapes.pill),
        contentAlignment = Alignment.Center,
    ) { Text(letter, style = TapTapType.label, color = on) }
}

@Preview(name = "All 8 themes", heightDp = 1500)
@Composable
private fun AllThemesPreview() {
    Column {
        ThemeColor.entries.forEach { color ->
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                TapTapTheme(color = color, mode = mode) {
                    TokenCard("${color.name.lowercase().replaceFirstChar { it.uppercase() }} ~ ${mode.name.lowercase().replaceFirstChar { it.uppercase() }}")
                }
            }
        }
    }
}

@Preview(name = "Counter colour override (app = Pink, counter = Teal)")
@Composable
private fun CounterThemePreview() {
    TapTapTheme(color = ThemeColor.PINK, mode = ThemeMode.LIGHT) {
        Column {
            TokenCard("App: Pink ~ Light")
            CounterTheme(ThemeColor.TEAL) {
                TokenCard("Counter: Teal accents, app's mode")
            }
        }
    }
}

@Preview(name = "Type scale", heightDp = 700)
@Composable
private fun TypeScalePreview() {
    TapTapTheme {
        val c = TapTapTheme.colors
        Column(
            Modifier.background(c.bg).padding(Spacing.space4),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            Text("32", style = TapTapType.countXl, color = c.ink)
            Text("14", style = TapTapType.countLg, color = c.ink)
            Text("32", style = TapTapType.stat, color = c.ink)
            Text("Tap Tap", style = TapTapType.titleLg, color = c.ink)
            Text("Quit cigarettes", style = TapTapType.title, color = c.ink)
            Text("নোট: সকালের জপ", style = TapTapType.body, color = c.ink)
            Text("हिन्दी में टेक्स्ट", style = TapTapType.body, color = c.ink)
            Text("Русский текст", style = TapTapType.body, color = c.ink)
            Text("Step value", style = TapTapType.label, color = c.ink)
            Text("7 days to your 21-day badge", style = TapTapType.caption, color = c.inkMuted)
        }
    }
}
