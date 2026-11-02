package com.taptap.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TimeUtilsTest {

    private val dhaka = ZoneId.of("Asia/Dhaka")
    private val newYork = ZoneId.of("America/New_York")

    private fun at(zone: ZoneId, y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0): Long =
        LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant().toEpochMilli()

    private val hour = 60L * 60L * 1000L
    private val day = 24 * hour

    // --- daysFree / hoursFree ---------------------------------------------------------

    @Test
    fun `days free counts whole elapsed days`() {
        val quit = at(dhaka, 2026, 10, 18, 9, 0)
        assertEquals(0, TimeUtils.daysFree(quit, quit))
        assertEquals(0, TimeUtils.daysFree(quit, quit + 23 * hour))
        assertEquals(1, TimeUtils.daysFree(quit, quit + day))
        assertEquals(14, TimeUtils.daysFree(quit, quit + 14 * day + 6 * hour))
    }

    @Test
    fun `hours free is the part beyond the whole days`() {
        val quit = at(dhaka, 2026, 10, 18, 9, 0)
        assertEquals(6, TimeUtils.hoursFree(quit, quit + 14 * day + 6 * hour))
        assertEquals(0, TimeUtils.hoursFree(quit, quit + 3 * day))
        assertEquals(23, TimeUtils.hoursFree(quit, quit + 23 * hour + 59 * 60 * 1000))
    }

    @Test
    fun `a quit date in the future never goes negative`() {
        val quit = at(dhaka, 2026, 11, 2)
        val before = quit - 5 * day
        assertEquals(0, TimeUtils.daysFree(quit, before))
        assertEquals(0, TimeUtils.hoursFree(quit, before))
    }

    // --- startOfToday -----------------------------------------------------------------

    @Test
    fun `start of today is local midnight`() {
        val now = at(dhaka, 2026, 11, 2, 15, 30)
        assertEquals(at(dhaka, 2026, 11, 2), TimeUtils.startOfToday(now, dhaka))
    }

    @Test
    fun `just before midnight still belongs to the old day`() {
        val almost = at(dhaka, 2026, 11, 2, 23, 59)
        assertEquals(at(dhaka, 2026, 11, 2), TimeUtils.startOfToday(almost, dhaka))
    }

    @Test
    fun `one minute past midnight starts the new day`() {
        val justAfter = at(dhaka, 2026, 11, 3, 0, 1)
        assertEquals(at(dhaka, 2026, 11, 3), TimeUtils.startOfToday(justAfter, dhaka))
    }

    @Test
    fun `the zone decides the boundary, not the server`() {
        // 2026-11-02 01:00 in Dhaka is still 2026-11-01 in New York.
        val now = at(dhaka, 2026, 11, 2, 1, 0)
        assertEquals(at(dhaka, 2026, 11, 2), TimeUtils.startOfToday(now, dhaka))
        assertEquals(at(newYork, 2026, 11, 1), TimeUtils.startOfToday(now, newYork))
    }

    @Test
    fun `a DST day still starts at local midnight`() {
        // US DST ends 2026-11-01; that local day is 25 hours long.
        val duringDstDay = at(newYork, 2026, 11, 1, 12, 0)
        assertEquals(at(newYork, 2026, 11, 1), TimeUtils.startOfToday(duringDstDay, newYork))
    }

    // --- milestones -------------------------------------------------------------------

    @Test
    fun `the milestone list is the ten days the requirements name`() {
        assertEquals(listOf(1, 3, 7, 14, 21, 30, 60, 90, 180, 365), TimeUtils.MILESTONES)
    }

    @Test
    fun `next milestone is the first one strictly greater`() {
        assertEquals(1, TimeUtils.nextMilestone(0))
        assertEquals(3, TimeUtils.nextMilestone(1))
        assertEquals(21, TimeUtils.nextMilestone(14))
        assertEquals(365, TimeUtils.nextMilestone(180))
    }

    @Test
    fun `there is no next milestone once 365 is reached`() {
        assertNull(TimeUtils.nextMilestone(365))
        assertNull(TimeUtils.nextMilestone(400))
    }

    @Test
    fun `previous milestone is zero before the first badge`() {
        assertEquals(0, TimeUtils.previousMilestone(0))
        assertEquals(1, TimeUtils.previousMilestone(1))
        assertEquals(14, TimeUtils.previousMilestone(20))
        assertEquals(365, TimeUtils.previousMilestone(400))
    }

    @Test
    fun `earned badge count grows with the days`() {
        assertEquals(0, TimeUtils.earnedBadgeCount(0))
        assertEquals(4, TimeUtils.earnedBadgeCount(14))
        assertEquals(10, TimeUtils.earnedBadgeCount(365))
        assertEquals(10, TimeUtils.earnedBadgeCount(1000))
    }

    // --- milestoneProgress ------------------------------------------------------------

    @Test
    fun `progress is zero exactly on a milestone`() {
        assertEquals(0f, TimeUtils.milestoneProgress(14, 0), 0.0001f)
    }

    @Test
    fun `progress is the fraction between the previous and next badge`() {
        // 14 -> 21 is a 7-day span; 17.5 days is half way.
        assertEquals(0.5f, TimeUtils.milestoneProgress(17, 12), 0.0001f)
        // 0 -> 1 is a one-day span; 12 hours is half way.
        assertEquals(0.5f, TimeUtils.milestoneProgress(0, 12), 0.0001f)
    }

    @Test
    fun `progress is full once every badge is earned`() {
        assertEquals(1f, TimeUtils.milestoneProgress(365, 0), 0.0001f)
        assertEquals(1f, TimeUtils.milestoneProgress(900, 5), 0.0001f)
    }

    @Test
    fun `days to next milestone matches the design's sentence`() {
        assertEquals(7, TimeUtils.daysToNextMilestone(14))
        assertEquals(1, TimeUtils.daysToNextMilestone(20))
        assertNull(TimeUtils.daysToNextMilestone(365))
    }

    // --- money ------------------------------------------------------------------------

    @Test
    fun `money saved is days times the daily spend`() {
        assertEquals(2100.0, TimeUtils.moneySaved(14, 150.0)!!, 0.0001)
        assertEquals(0.0, TimeUtils.moneySaved(0, 150.0)!!, 0.0001)
    }

    @Test
    fun `no money per day means no money saved`() {
        assertNull(TimeUtils.moneySaved(14, null))
    }
}
