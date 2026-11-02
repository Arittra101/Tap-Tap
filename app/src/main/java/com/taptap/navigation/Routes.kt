package com.taptap.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every screen in the app, as a back stack key.
 *
 * Keys carry ids only, never whole objects — the screen that opens loads its own data
 * (tech spec 5). Keys are suffixed `Key` and the composables that render them `Route`, to
 * settle the name clash the spec flags in 5.2.
 */
@Serializable
data object HomeKey : NavKey

/**
 * [startAsQuit] preselects the Quit form. Home's first-run welcome offers "Normal
 * counter" and "Quit counter" as separate choices, so each must open its own form.
 */
@Serializable
data class CreateKey(val startAsQuit: Boolean = false) : NavKey

@Serializable
data class CounterKey(val counterId: Long) : NavKey

@Serializable
data class QuitKey(val counterId: Long) : NavKey

@Serializable
data class EditKey(val counterId: Long) : NavKey

@Serializable
data object SettingsKey : NavKey
