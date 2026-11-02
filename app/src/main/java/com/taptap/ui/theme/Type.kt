package com.taptap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.taptap.R

/**
 * Tap Tap's type scale, from the design system's `tokens.json` -> `type`.
 *
 * FR-7.5: Outfit for numbers and titles, Noto Sans for text, and tabular figures
 * everywhere a number appears so the count does not jump width while you tap.
 *
 * Both faces are bundled rather than downloaded, because the app is fully offline
 * (NFR-1) — a first launch with no network still shows the real typefaces.
 *
 * Bangla and Devanagari: Android picks Noto Sans Bengali / Devanagari from the system
 * fallback chain for glyphs the bundled faces do not cover, which is the same stack the
 * design specifies.
 */

private fun weightAxis(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))

/** Display family — numbers, the app name, screen titles. */
val Outfit = FontFamily(
    Font(R.font.outfit_variable, FontWeight.Normal, variationSettings = weightAxis(400)),
    Font(R.font.outfit_variable, FontWeight.Medium, variationSettings = weightAxis(500)),
    Font(R.font.outfit_variable, FontWeight.SemiBold, variationSettings = weightAxis(600)),
    Font(R.font.outfit_variable, FontWeight.Bold, variationSettings = weightAxis(700)),
)

/** Text family — everything that is not a number or a title. */
val NotoSans = FontFamily(
    Font(R.font.noto_sans_variable, FontWeight.Normal, variationSettings = weightAxis(400)),
    Font(R.font.noto_sans_variable, FontWeight.Medium, variationSettings = weightAxis(500)),
    Font(R.font.noto_sans_variable, FontWeight.SemiBold, variationSettings = weightAxis(600)),
    Font(R.font.noto_sans_variable, FontWeight.Bold, variationSettings = weightAxis(700)),
)

/** Numbers never shift width while counting (FR-7.5). */
private const val TABULAR = "tnum"

/**
 * The nine named styles. Sizes are in sp so they follow the system font setting (NFR-7);
 * line heights are in sp for the same reason.
 */
@Suppress("MemberVisibilityCanBePrivate")
object TapTapType {

    /** 104sp — the number inside the counter dial. */
    val countXl = TextStyle(
        fontFamily = Outfit,
        fontSize = 104.sp,
        lineHeight = 108.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.029).em,
        fontFeatureSettings = TABULAR,
    )

    /** 72sp — days free on the Focus card. */
    val countLg = TextStyle(
        fontFamily = Outfit,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.028).em,
        fontFeatureSettings = TABULAR,
    )

    /** 34sp — the count on a normal counter card, days free on a quit card. */
    val stat = TextStyle(
        fontFamily = Outfit,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TABULAR,
    )

    /** 22sp — the app name in the Home top bar. */
    val titleLg = TextStyle(
        fontFamily = Outfit,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** 20sp — screen titles in an inner top bar, in the display family. */
    val screenTitle = TextStyle(
        fontFamily = Outfit,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    /** 17sp — counter names, sheet titles, section headers. */
    val title = TextStyle(
        fontFamily = NotoSans,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** 15sp — notes, settings row labels, field values. */
    val body = TextStyle(
        fontFamily = NotoSans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    )

    /** 13sp — buttons, chips, field labels, tabs. */
    val label = TextStyle(
        fontFamily = NotoSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.015.em,
    )

    /** 12sp — helper text and meta lines, in `inkMuted`. */
    val caption = TextStyle(
        fontFamily = NotoSans,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    )

    /** A stat-strip value: the display face at 20-22sp with tabular figures. */
    val statValue = TextStyle(
        fontFamily = Outfit,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TABULAR,
    )
}

/**
 * Material 3's scale, mapped onto the design's styles so a stock Material component
 * inherits Tap Tap's type instead of Roboto.
 */
val TapTapTypography = Typography(
    displayLarge = TapTapType.countXl,
    displayMedium = TapTapType.countLg,
    displaySmall = TapTapType.stat,
    headlineLarge = TapTapType.titleLg,
    headlineMedium = TapTapType.screenTitle,
    headlineSmall = TapTapType.title,
    titleLarge = TapTapType.titleLg,
    titleMedium = TapTapType.title,
    titleSmall = TapTapType.label,
    bodyLarge = TapTapType.body,
    bodyMedium = TapTapType.body,
    bodySmall = TapTapType.caption,
    labelLarge = TapTapType.label,
    labelMedium = TapTapType.label,
    labelSmall = TapTapType.caption,
)
