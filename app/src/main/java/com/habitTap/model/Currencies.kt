package com.habitTap.model

/**
 * The currencies Settings offers for the money a quit counter saves.
 *
 * A short list rather than all of ISO 4217: it covers the app's six languages and the
 * markets around them, and stays readable without a search field. [FOLLOW_DEVICE] is the
 * default and means "whatever the phone's region uses", which is right for almost
 * everyone — the list is for the people it is wrong for, who have moved country or whose
 * phone is set to one region and their money to another.
 *
 * Only the code is stored. The name, the symbol and where that symbol belongs in the
 * number all come from [java.util.Currency] in the user's own language, so adding a
 * currency here needs no new strings and no new translations.
 */
object Currencies {

    /** An empty stored code: follow the phone's region. */
    const val FOLLOW_DEVICE = ""

    /** In the order Settings lists them. */
    val codes = listOf(
        "BDT",
        "INR",
        "USD",
        "EUR",
        "GBP",
        "RUB",
        "PKR",
        "IDR",
        "NGN",
        "BRL",
        "JPY",
        "AED",
    )

    /**
     * The glyph to use when the display language does not know one.
     *
     * `Currency.getSymbol` falls back to the three-letter code — an English phone set to
     * taka showed "BDT0" — so these fill in for the currencies this app offers. Where the
     * language *does* know a symbol, that one wins: it is the one that language's readers
     * expect.
     */
    val symbols = mapOf(
        "BDT" to "৳",
        "INR" to "₹",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "RUB" to "₽",
        "PKR" to "₨",
        "IDR" to "Rp",
        "NGN" to "₦",
        "BRL" to "R$",
        "JPY" to "¥",
        "AED" to "د.إ",
    )

    /** A stored value the app still understands, or [FOLLOW_DEVICE] if it does not. */
    fun sanitize(code: String?): String = code?.takeIf { it in codes } ?: FOLLOW_DEVICE
}
