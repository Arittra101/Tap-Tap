package com.taptap.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taptap.model.EventKind

/**
 * One row per thing that happened to a counter.
 *
 * It powers "Today" (FR-3.2) and urges beaten (FR-4.4) now. A stats screen is out of
 * scope for v1 (requirements 8), but the log is written from the start so adding one
 * later needs no migration of lost history.
 */
@Entity(
    tableName = "counter_events",
    foreignKeys = [
        ForeignKey(
            entity = CounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["counterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("counterId"), Index("at")],
)
data class CounterEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,
    val kind: EventKind,
    /** The delta actually applied: +step or -step for a TAP, 0 otherwise. */
    val delta: Long = 0,
    val at: Long,
)
