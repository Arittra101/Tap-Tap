package com.taptap.model

/** Normal counts anything; Quit stays away from a habit, day by day (requirements 2). */
enum class CounterType { NORMAL, QUIT }

/** The four colour themes (FR-6.1). Also a counter's own colour (FR-5.3). */
enum class ThemeColor { PINK, TEAL, SAFFRON, BLUE }

/**
 * Light, Dark, or follow the phone (FR-6.2). SYSTEM is not a theme: it picks the light or
 * dark theme of the chosen colour from the phone setting.
 */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** The default counter backgrounds (FR-5.2). Drawn as vectors, not bundled images. */
enum class Background { NONE, WAVES, ORBS, HILLS }

/** What a row in the event log records (requirements 6). */
enum class EventKind { TAP, URGE_BEATEN, RESET, SLIP }
