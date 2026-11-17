package com.habitTap.util

import com.habitTap.model.Currencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Money is a language and a currency, independently (FR-8.3).
 *
 * The locale decides the digits, the grouping and which side the symbol sits on; the
 * chosen currency decides which symbol. Someone reading the app in Bangla with their money
 * in pounds should see a pound sign and Bangla digits, not one or the other.
 */
class FormattersTest {

    @Test
    fun `an empty code uses the locale's own currency`() {
        assertTrue(Formatters.money(1500.0, Locale.US).contains("$"))
        assertTrue(Formatters.money(1500.0, Locale("en", "IN")).contains("₹"))
    }

    @Test
    fun `a chosen currency overrides the locale's`() {
        val money = Formatters.money(1500.0, Locale.US, "INR")
        assertTrue("expected a rupee amount, got $money", money.contains("₹"))
    }

    @Test
    fun `the locale still decides how the number reads`() {
        // Same currency, two languages: Bangla writes the digits its own way.
        val english = Formatters.money(1500.0, Locale.UK, "GBP")
        val bangla = Formatters.money(1500.0, Locale("bn", "BD"), "GBP")
        assertTrue(english.contains("£"))
        assertTrue(bangla.contains("£"))
        assertTrue("Bangla digits expected in $bangla", bangla.any { it in '০'..'৯' })
    }

    @Test
    fun `money carries no decimals`() {
        assertEquals("these are whole amounts", "$1,500", Formatters.money(1500.4, Locale.US, "USD"))
    }

    @Test
    fun `an unknown code is ignored rather than thrown`() {
        // Not reachable through Settings, which sanitises, but formatting must not crash
        // on a value that somehow got stored.
        val money = Formatters.money(100.0, Locale.US, "NOPE")
        assertTrue("expected the locale's currency, got $money", money.contains("$"))
    }

    @Test
    fun `a currency with no symbol in this language still gets a glyph`() {
        // An English phone set to taka used to read "BDT0".
        val money = Formatters.money(0.0, Locale.US, "BDT")
        assertTrue("expected a taka glyph, got $money", money.contains("৳"))
        assertTrue("the bare code should not be shown, got $money", !money.contains("BDT"))
    }

    @Test
    fun `the display language's own symbol wins over the fallback glyph`() {
        // en-IN knows ₹ for itself; the map must not override what the language knows.
        assertEquals("₹", Formatters.currencySymbol(Locale("en", "IN"), "INR"))
    }

    @Test
    fun `a symbol is offered for every currency the picker lists`() {
        Currencies.codes.forEach { code ->
            val symbol = Formatters.currencySymbol(Locale.US, code)
            assertTrue("no symbol for $code", symbol.isNotEmpty())
            assertTrue("no name for $code", Formatters.currencyName(code, Locale.US).isNotEmpty())
        }
    }

    @Test
    fun `a locale with no country has no currency of its own and does not crash`() {
        // Locale("bn") alone has no region, so java.util.Currency cannot resolve one.
        assertEquals("", Formatters.currencySymbol(Locale("bn")))
    }
}
