package com.habitTap.model

/**
 * The stored preferences (requirements 6, spec 7.4).
 *
 * [languageTag] is empty when the user has not chosen one, which means "follow the phone
 * if it speaks one of the six languages, else English".
 */
data class AppSettings(
    val themeColor: ThemeColor = ThemeColor.SAFFRON,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "",
    /**
     * The currency for the money a quit counter saves, as an ISO code. Empty means follow
     * the phone's region, which is the default.
     */
    val currencyCode: String = Currencies.FOLLOW_DEVICE,
    val vibration: Boolean = true,
    val sound: Boolean = false,
    /**
     * The first-run tutorial has been finished or skipped. Not a user-facing setting —
     * it is stored here because this is where the app already keeps what it remembers
     * between launches.
     *
     * The default is `false`, so read it from the store and not from
     * [com.habitTap.data.repository.SettingsRepository.settings], whose cache is seeded
     * with these defaults and would say "not seen" to someone who has already seen it.
     */
    val tutorialSeen: Boolean = false,
    /**
     * The guided journey on Home has been finished or stopped. Stored for the same reason
     * as [tutorialSeen], and read the same way — from the store, never from the cache.
     */
    val journeyDone: Boolean = false,
)
