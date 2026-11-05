package com.habitTap.model

/**
 * The five stored preferences (requirements 6, spec 7.4).
 *
 * [languageTag] is empty when the user has not chosen one, which means "follow the phone
 * if it speaks one of the six languages, else English".
 */
data class AppSettings(
    val themeColor: ThemeColor = ThemeColor.PINK,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "",
    val vibration: Boolean = true,
    val sound: Boolean = false,
)
