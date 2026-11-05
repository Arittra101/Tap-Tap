package com.habitTap.data.db

import androidx.room.TypeConverter
import com.habitTap.model.Background
import com.habitTap.model.CounterType
import com.habitTap.model.EventKind
import com.habitTap.model.ThemeColor

/**
 * Enums are stored as their `name`, not their ordinal, so reordering an enum can never
 * silently repoint existing rows (tech spec 7.1).
 *
 * A value that no longer parses falls back rather than throwing: a database written by a
 * newer build should not crash an older one.
 */
class Converters {

    @TypeConverter
    fun counterTypeToString(value: CounterType): String = value.name

    @TypeConverter
    fun stringToCounterType(value: String): CounterType =
        enumValues<CounterType>().firstOrNull { it.name == value } ?: CounterType.NORMAL

    @TypeConverter
    fun themeColorToString(value: ThemeColor?): String? = value?.name

    @TypeConverter
    fun stringToThemeColor(value: String?): ThemeColor? =
        value?.let { name -> enumValues<ThemeColor>().firstOrNull { it.name == name } }

    @TypeConverter
    fun backgroundToString(value: Background): String = value.name

    @TypeConverter
    fun stringToBackground(value: String): Background =
        enumValues<Background>().firstOrNull { it.name == value } ?: Background.NONE

    @TypeConverter
    fun eventKindToString(value: EventKind): String = value.name

    @TypeConverter
    fun stringToEventKind(value: String): EventKind =
        enumValues<EventKind>().firstOrNull { it.name == value } ?: EventKind.TAP
}
