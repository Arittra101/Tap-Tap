package com.taptap.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.taptap.data.db.AppDatabase
import com.taptap.data.db.CounterDao
import com.taptap.model.Background
import com.taptap.model.Counter
import com.taptap.model.CounterType
import com.taptap.model.ThemeColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The rules from tech spec 12: tap, stop at target, reverse never below zero, reset to
 * the start value, one focus only, slip keeps the best streak. Plus the NFR-2 burst.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CounterRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CounterDao
    private lateinit var repo: CounterRepository

    /** A clock the tests move by hand, so nothing waits on real time. */
    private var now: Long = 1_762_000_000_000L

    private val hour = 60L * 60L * 1000L
    private val day = 24 * hour

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.counterDao()
        repo = CounterRepository(dao) { now }
    }

    @After
    fun tearDown() = db.close()

    // --- fixtures ---------------------------------------------------------------------

    private fun normal(
        name: String = "Push-ups",
        count: Long = 0,
        startValue: Long = 0,
        step: Int = 1,
        target: Long? = null,
        keepCounting: Boolean = false,
        locked: Boolean = false,
        reversed: Boolean = false,
    ) = Counter(
        id = 0, type = CounterType.NORMAL, name = name, notes = null, color = null,
        background = Background.NONE, isPinned = false, isFocus = false, createdAt = now,
        sortOrder = 0, count = count, startValue = startValue, step = step, target = target,
        keepCountingAfterTarget = keepCounting, isLocked = locked, isReversed = reversed,
        quitAt = null, moneyPerDay = null, goalDays = null, reason = null,
        bestStreakDays = 0, badgesOn = true, dailyMessageOn = true, dailyMessageMinute = 540,
    )

    private fun quit(
        name: String = "Quit cigarettes",
        quitAt: Long = now,
        isFocus: Boolean = false,
        bestStreak: Int = 0,
    ) = Counter(
        id = 0, type = CounterType.QUIT, name = name, notes = null, color = null,
        background = Background.NONE, isPinned = false, isFocus = isFocus, createdAt = now,
        sortOrder = 0, count = 0, startValue = 0, step = 1, target = null,
        keepCountingAfterTarget = false, isLocked = false, isReversed = false,
        quitAt = quitAt, moneyPerDay = 150.0, goalDays = 30, reason = "To breathe better",
        bestStreakDays = bestStreak, badgesOn = true, dailyMessageOn = true,
        dailyMessageMinute = 540,
    )

    private suspend fun countOf(id: Long) = repo.getCounter(id)!!.count

    // --- tap --------------------------------------------------------------------------

    @Test
    fun `a tap adds the step value`() = runTest {
        val id = repo.create(normal(step = 5))
        repo.tap(id)
        assertEquals(5L, countOf(id))
        repo.tap(id)
        assertEquals(10L, countOf(id))
    }

    @Test
    fun `FR-3_4 a locked counter ignores taps and logs nothing`() = runTest {
        val id = repo.create(normal(count = 7, locked = true))
        assertEquals(0L, repo.tap(id))
        assertEquals(7L, countOf(id))
        assertEquals(0, dao.countEvents(id))
    }

    @Test
    fun `FR-3_5 reverse subtracts the step`() = runTest {
        val id = repo.create(normal(count = 10, step = 3, reversed = true))
        repo.tap(id)
        assertEquals(7L, countOf(id))
    }

    @Test
    fun `FR-3_5 reverse never goes below zero`() = runTest {
        val id = repo.create(normal(count = 2, step = 5, reversed = true))
        repo.tap(id)
        assertEquals(0L, countOf(id))
        // A tap that would change nothing writes nothing.
        assertEquals(0L, repo.tap(id))
        assertEquals(0L, countOf(id))
        assertEquals(1, dao.countEvents(id))
    }

    @Test
    fun `FR-3_7 the count stops at the target when keep counting is off`() = runTest {
        val id = repo.create(normal(count = 48, step = 5, target = 50))
        repo.tap(id)
        assertEquals(50L, countOf(id))
        assertEquals(0L, repo.tap(id))
        assertEquals(50L, countOf(id))
    }

    @Test
    fun `FR-3_7 keep counting carries past the target`() = runTest {
        val id = repo.create(normal(count = 48, step = 5, target = 50, keepCounting = true))
        repo.tap(id)
        assertEquals(53L, countOf(id))
    }

    @Test
    fun `a counter at its target can still count down`() = runTest {
        val id = repo.create(normal(count = 50, step = 5, target = 50, reversed = true))
        repo.tap(id)
        assertEquals(45L, countOf(id))
    }

    @Test
    fun `the event log records the delta actually applied`() = runTest {
        // Only 2 of the 5-step tap fits under the target, so that is what is logged.
        val id = repo.create(normal(count = 48, step = 5, target = 50))
        repo.tap(id)
        val change = repo.observeChangeSince(id, now - day).first()
        assertEquals(2L, change)
    }

    // --- NFR-2 ------------------------------------------------------------------------

    @Test
    fun `NFR-2 a burst of taps loses none of them`() = runTest {
        val id = repo.create(normal(step = 1))
        withContext(Dispatchers.Default) {
            (1..200).map { async { repo.tap(id) } }.awaitAll()
        }
        assertEquals(200L, countOf(id))
        assertEquals(200, dao.countEvents(id))
    }

    // --- reset ------------------------------------------------------------------------

    @Test
    fun `FR-3_6 reset goes to the start value, not to zero`() = runTest {
        val id = repo.create(normal(count = 37, startValue = 10))
        repo.reset(id)
        assertEquals(10L, countOf(id))
    }

    // --- quit -------------------------------------------------------------------------

    @Test
    fun `FR-4_4 beating an urge does not change days free`() = runTest {
        val quitAt = now - 14 * day
        val id = repo.create(quit(quitAt = quitAt))
        repo.beatUrge(id)
        repo.beatUrge(id)
        assertEquals(quitAt, repo.getCounter(id)!!.quitAt)
        assertEquals(2, repo.observeUrgesTotal(id).first())
        assertEquals(2, repo.observeUrgesSince(id, now - day).first())
    }

    @Test
    fun `FR-4_8 a slip restarts the days and keeps the best streak`() = runTest {
        val id = repo.create(quit(quitAt = now - 21 * day, bestStreak = 5))
        repo.beatUrge(id)
        repo.slip(id)

        val after = repo.getCounter(id)!!
        assertEquals(now, after.quitAt)
        assertEquals(21, after.bestStreakDays)
        // Urges are kept.
        assertEquals(1, repo.observeUrgesTotal(id).first())
    }

    @Test
    fun `FR-4_8 a slip never lowers an older best streak`() = runTest {
        val id = repo.create(quit(quitAt = now - 3 * day, bestStreak = 40))
        repo.slip(id)
        assertEquals(40, repo.getCounter(id)!!.bestStreakDays)
    }

    // --- focus ------------------------------------------------------------------------

    @Test
    fun `FR-2_6 only one counter is the focus`() = runTest {
        val a = repo.create(quit(name = "Quit cigarettes", isFocus = true))
        val b = repo.create(quit(name = "Quit soft drinks"))
        assertTrue(repo.getCounter(a)!!.isFocus)

        repo.setFocus(b)
        assertFalse(repo.getCounter(a)!!.isFocus)
        assertTrue(repo.getCounter(b)!!.isFocus)
        assertEquals(1, repo.observeAll().first().count { it.isFocus })
    }

    @Test
    fun `FR-2_6 creating a focus counter takes focus from the old one`() = runTest {
        val a = repo.create(quit(name = "A", isFocus = true))
        val b = repo.create(quit(name = "B", isFocus = true))
        assertFalse(repo.getCounter(a)!!.isFocus)
        assertTrue(repo.getCounter(b)!!.isFocus)
    }

    @Test
    fun `a normal counter can never be the focus`() = runTest {
        val n = repo.create(normal())
        repo.setFocus(n)
        assertFalse(repo.getCounter(n)!!.isFocus)
    }

    // --- pins and ordering ------------------------------------------------------------

    @Test
    fun `FR-1_5 pinned counters come first`() = runTest {
        repo.create(normal(name = "First"))
        val second = repo.create(normal(name = "Second"))
        repo.togglePin(second)
        assertEquals(listOf("Second", "First"), repo.observeAll().first().map { it.name })
    }

    // --- edit -------------------------------------------------------------------------

    @Test
    fun `FR-5_4 changing step and start value never moves the current count`() = runTest {
        val id = repo.create(normal(count = 0, step = 1, startValue = 0))
        repeat(7) { repo.tap(id) }
        assertEquals(7L, countOf(id))

        val edited = repo.getCounter(id)!!.copy(step = 5, startValue = 10, count = 999)
        repo.update(edited)

        val after = repo.getCounter(id)!!
        assertEquals("the count must not jump", 7L, after.count)
        assertEquals(5, after.step)
        assertEquals(10L, after.startValue)

        // The new step applies from the next tap, the new start value from the next reset.
        repo.tap(id)
        assertEquals(12L, countOf(id))
        repo.reset(id)
        assertEquals(10L, countOf(id))
    }

    // --- delete -----------------------------------------------------------------------

    @Test
    fun `FR-5_7 deleting a counter removes its events too`() = runTest {
        val id = repo.create(normal())
        repeat(3) { repo.tap(id) }
        assertEquals(3, dao.countEvents(id))

        repo.delete(id)
        assertNull(repo.getCounter(id))
        assertEquals(0, dao.countEvents(id))
    }

    // --- today ------------------------------------------------------------------------

    @Test
    fun `FR-3_2 today's change counts only taps since the given instant`() = runTest {
        val id = repo.create(normal(step = 1))
        repeat(4) { repo.tap(id) }   // yesterday
        val midnight = now + hour
        now += 2 * hour              // today
        repeat(3) { repo.tap(id) }

        assertEquals(3L, repo.observeChangeSince(id, midnight).first())
        assertEquals(7L, countOf(id))
    }

    @Test
    fun `a missing counter is handled quietly`() = runTest {
        assertEquals(0L, repo.tap(999))
        repo.reset(999)
        repo.slip(999)
        repo.beatUrge(999)
        assertNull(repo.getCounter(999))
        assertNotNull(repo.observeAll().first())
    }
}
