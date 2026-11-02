package com.taptap.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Counters and their event log (NFR-1: everything is on the phone).
 *
 * `exportSchema` is on and the generated JSON is committed, so a later migration can be
 * written against a known starting point (tech spec 7.1).
 */
@Database(
    entities = [CounterEntity::class, CounterEventEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao

    companion object {
        const val NAME = "taptap.db"
    }
}
