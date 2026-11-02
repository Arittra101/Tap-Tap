package com.taptap.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Plain queries only. Every business rule lives in `CounterRepository` instead, so Home's
 * `+` button and the counter screen's dial cannot drift apart (tech spec 4.3).
 */
@Dao
interface CounterDao {

    /** Pinned first inside the whole list; callers split by type (FR-1.5, FR-1.7). */
    @Query("SELECT * FROM counters ORDER BY isPinned DESC, sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE id = :id")
    fun observeById(id: Long): Flow<CounterEntity?>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getById(id: Long): CounterEntity?

    @Query("SELECT * FROM counters WHERE type = 'QUIT' AND dailyMessageOn = 1")
    suspend fun getQuitCountersWithDailyMessage(): List<CounterEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM counters WHERE type = :type")
    suspend fun nextSortOrder(type: String): Int

    @Insert
    suspend fun insert(counter: CounterEntity): Long

    @Update
    suspend fun update(counter: CounterEntity)

    @Query("DELETE FROM counters WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE counters SET isFocus = 0 WHERE isFocus = 1")
    suspend fun clearFocus()

    @Query("UPDATE counters SET isFocus = 1 WHERE id = :id")
    suspend fun setFocusFlag(id: Long)

    /**
     * One transaction, so there is never a moment where two counters are both the focus
     * or none is (FR-2.6).
     */
    @Transaction
    suspend fun setFocus(id: Long) {
        clearFocus()
        setFocusFlag(id)
    }

    @Insert
    suspend fun insertEvent(event: CounterEventEntity)

    /** Today's change: the sum of tap deltas since local midnight (FR-3.2). */
    @Query(
        "SELECT COALESCE(SUM(delta), 0) FROM counter_events " +
            "WHERE counterId = :id AND kind = 'TAP' AND at >= :since"
    )
    fun observeChangeSince(id: Long, since: Long): Flow<Long>

    /** Urges beaten today (FR-1.2, FR-4.4). */
    @Query(
        "SELECT COUNT(*) FROM counter_events " +
            "WHERE counterId = :id AND kind = 'URGE_BEATEN' AND at >= :since"
    )
    fun observeUrgesSince(id: Long, since: Long): Flow<Int>

    /** Total urges beaten, all time (FR-4.3). */
    @Query("SELECT COUNT(*) FROM counter_events WHERE counterId = :id AND kind = 'URGE_BEATEN'")
    fun observeUrgesTotal(id: Long): Flow<Int>

    /**
     * Urges beaten today for every counter at once.
     *
     * Home shows this on the Focus card and on each quit card, so one grouped query beats
     * one flow per counter.
     */
    @Query(
        "SELECT counterId, COUNT(*) AS urges FROM counter_events " +
            "WHERE kind = 'URGE_BEATEN' AND at >= :since GROUP BY counterId"
    )
    fun observeUrgesTodayAll(since: Long): Flow<List<UrgeCount>>

    @Query("SELECT COUNT(*) FROM counter_events WHERE counterId = :id")
    suspend fun countEvents(id: Long): Int
}

/** One row of [CounterDao.observeUrgesTodayAll]. */
data class UrgeCount(val counterId: Long, val urges: Int)
