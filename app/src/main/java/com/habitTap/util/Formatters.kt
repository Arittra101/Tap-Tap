package com.habitTap.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.habitTap.model.Currencies
import java.text.DecimalFormat
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
 * Numbers and dates follow the chosen language, so Bangla shows Bangla digits. Money uses
 * the currency chosen in Settings, or the device's when that is left to follow the phone.
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

    /**
     * Money with no decimals — these are whole amounts.
     *
     * [currencyCode] empty means the device's own currency. The locale still decides
     * grouping, digits and which side the symbol sits on, so a rupee amount reads the way
     * a rupee amount should in whichever language the app is in.
     */
    fun money(amount: Double, locale: Locale, currencyCode: String = ""): String {
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
            currencyOrNull(currencyCode, locale)?.let { currency = it }
        }
        // Setting the currency alone can leave the bare code as the symbol — "BDT0" on an
        // English phone. The symbol is replaced after it, never before: setting the
        // currency resets the symbols.
        val symbol = currencySymbol(locale, currencyCode)
        if (format is DecimalFormat && symbol.isNotEmpty()) {
            format.decimalFormatSymbols = format.decimalFormatSymbols.apply {
                currencySymbol = symbol
            }
        }
        return format.format(amount)
    }

    /**
     * The currency symbol, for a money field's prefix and for formatting.
     *
     * The display language's own symbol when it has one, and the glyph from
     * [Currencies.symbols] when it would otherwise hand back the three-letter code.
     */
    fun currencySymbol(locale: Locale, currencyCode: String = ""): String {
        val currency = currencyOrNull(currencyCode, locale) ?: return ""
        val symbol = currency.getSymbol(locale)
        return if (symbol == currency.currencyCode) {
            Currencies.symbols[currency.currencyCode] ?: symbol
        } else {
            symbol
        }
    }

    /** The currency's name in the user's language, for the Settings list. */
    fun currencyName(currencyCode: String, locale: Locale): String =
        currencyOrNull(currencyCode, locale)?.getDisplayName(locale).orEmpty()

    /**
     * The chosen currency, the device's when none is chosen, or null when neither can be
     * resolved — a locale with no country attached has no currency, and asking for one
     * throws rather than returning null.
     */
    private fun currencyOrNull(currencyCode: String, locale: Locale): Currency? = runCatching {
        if (currencyCode.isEmpty()) Currency.getInstance(locale) else Currency.getInstance(currencyCode)
    }.getOrNull()

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

/**
 * The currency chosen in Settings, or empty for the device's own.
 *
 * A composition local rather than a parameter: money is drawn on four screens that
 * otherwise have no reason to know about settings, and a preview with no provider shows
 * the device's currency, which is what a preview should show.
 */
val LocalCurrencyCode = compositionLocalOf { "" }

/** The currency money should be drawn in on this screen. */
@Composable
fun currencyCode(): String = LocalCurrencyCode.current

/** The currency symbol to show, for a money field's prefix. */
@Composable
fun currencySymbol(): String {
    val locale = currentLocale()
    val code = LocalCurrencyCode.current
    return remember(locale, code) { Formatters.currencySymbol(locale, code) }
}

/** An amount in the chosen currency and the current locale. */
@Composable
fun localMoney(amount: Double): String {
    val locale = currentLocale()
    val code = LocalCurrencyCode.current
    return remember(amount, locale, code) { Formatters.money(amount, locale, code) }
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
