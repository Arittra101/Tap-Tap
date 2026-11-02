package com.taptap.util

import java.time.Instant
import java.time.ZoneId
import kotlin.math.floor

/**
 * The day arithmetic behind quit counters and the "Today" stat.
 *
 * Every function is pure and takes `now` as a parameter, so the behaviour is testable and
 * a clock change cannot surprise a caller (tech spec 10, 12).
 */
object TimeUtils {

    /** The ten milestone days a quit counter earns badges at (requirements 2, FR-4.6). */
    val MILESTONES: List<Int> = listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365)

    private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
    private const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR

    /**
     * Whole days elapsed since the quit instant (FR-4.2).
     *
     * This is elapsed time, not calendar days: a counter started at 18:00 yesterday is
     * 0 days free this morning, which is what the quit ring's "0 d 14 h" line says.
     */
    fun daysFree(quitAt: Long, now: Long): Int {
        val elapsed = (now - quitAt).coerceAtLeast(0L)
        return (elapsed / MILLIS_PER_DAY).toInt()
    }

    /** The hours part beyond the whole days, for "14 d 6 h without it". */
    fun hoursFree(quitAt: Long, now: Long): Int {
        val elapsed = (now - quitAt).coerceAtLeast(0L)
        return ((elapsed % MILLIS_PER_DAY) / MILLIS_PER_HOUR).toInt()
    }

    /** Total whole hours elapsed, for the rare case a caller wants them unsplit. */
    fun totalHoursFree(quitAt: Long, now: Long): Long =
        (now - quitAt).coerceAtLeast(0L) / MILLIS_PER_HOUR

    /**
     * Local midnight at the start of the day containing [now] (TT-031 decision 2).
     *
     * The zone is a parameter rather than a cached field, so a device that crosses a
     * timezone — or a user who picks their own day boundary later — needs no change here.
     */
    fun startOfToday(now: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(now)
            .atZone(zone)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

    /** The next badge after [days], or null once all ten are earned (FR-4.6). */
    fun nextMilestone(days: Int): Int? = MILESTONES.firstOrNull { it > days }

    /** The last badge earned at [days], or 0 before the first one. */
    fun previousMilestone(days: Int): Int = MILESTONES.lastOrNull { it <= days } ?: 0

    /** How many of the ten badges are earned at [days]. */
    fun earnedBadgeCount(days: Int): Int = MILESTONES.count { it <= days }

    /**
     * Progress from the previous badge to the next one, 0..1 (FR-4.2).
     *
     * Note this measures the gap between two badges, not progress toward the user's goal
     * — the QuitCard and the quit ring both fill toward the next badge.
     */
    fun milestoneProgress(days: Int, hours: Int = 0): Float {
        val next = nextMilestone(days) ?: return 1f
        val prev = previousMilestone(days)
        val span = (next - prev).toFloat()
        if (span <= 0f) return 1f
        val done = (days - prev) + (hours.coerceIn(0, 23) / 24f)
        return (done / span).coerceIn(0f, 1f)
    }

    /** Days left until the next badge, or null once all ten are earned. */
    fun daysToNextMilestone(days: Int): Int? = nextMilestone(days)?.let { it - days }

    /** Money saved so far: days free x money per day (FR-4.3). Null when no money is set. */
    fun moneySaved(daysFree: Int, moneyPerDay: Double?): Double? =
        moneyPerDay?.let { floor(daysFree.coerceAtLeast(0) * it * 100) / 100 }
}
