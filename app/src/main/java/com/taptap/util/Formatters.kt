package com.taptap.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

/**
 * Locale-aware formatting (FR-8.3).
 *
 * Numbers and dates follow the chosen language, so Bangla shows Bangla digits, and money
 * uses the **device's** currency symbol.
 *
 * Note the design canvas hard-codes a taka symbol with Indian grouping
 * (`'৳' + n.toLocaleString('en-IN')`). That is a mockup convenience: FR-8.3 asks for
 * the local currency, so the symbol here comes from the locale and the taka only appears
 * for users whose locale uses it.
 */
object Formatters {

    /** A plain count in the current locale's digits. */
    fun number(value: Long, locale: Locale): String =
        NumberFormat.getIntegerInstance(locale).format(value)

    fun number(value: Int, locale: Locale): String = number(value.toLong(), locale)

    /** A signed change, for the "Today" stat: "+12" / "-3" / "0". */
    fun signed(value: Long, locale: Locale): String {
        val formatted = NumberFormat.getIntegerInstance(locale).format(kotlin.math.abs(value))
        return when {
            value > 0 -> "+$formatted"
            value < 0 -> "-$formatted"
            else -> formatted
        }
    }

    fun percent(fraction: Float, locale: Locale): String =
        NumberFormat.getPercentInstance(locale).format(fraction.coerceIn(0f, 1f))

    /** Money in the device locale's currency, with no decimals — these are whole amounts. */
    fun money(amount: Double, locale: Locale): String {
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        return format.format(amount)
    }

    /** Just the currency symbol, for a text field's prefix. */
    fun currencySymbol(locale: Locale): String = runCatching {
        Currency.getInstance(locale).getSymbol(locale)
    }.getOrElse { "" }

    /** A medium date, for "Free since 18 Oct 2026". */
    fun date(millis: Long, locale: Locale, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(zone)
            .format(Instant.ofEpochMilli(millis))
}

/** The locale Compose is currently rendering in. */
@Composable
fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
    }
}

/** The currency symbol for the current locale, for a money field's prefix. */
@Composable
fun currencySymbol(): String {
    val locale = currentLocale()
    return remember(locale) { Formatters.currencySymbol(locale) }
}

/**
 * A number in the current locale's digits — Bangla digits in Bangla (FR-8.3).
 *
 * Resource strings with a `%d` are localised by Android automatically; a raw
 * `Long.toString()` is not, which is why every number drawn directly goes through here.
 */
@Composable
fun localNumber(value: Long): String {
    val locale = currentLocale()
    return remember(value, locale) { Formatters.number(value, locale) }
}

@Composable
fun localNumber(value: Int): String = localNumber(value.toLong())
