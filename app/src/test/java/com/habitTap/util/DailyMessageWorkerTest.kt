package com.habitTap.util

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.habitTap.data.db.AppDatabase
import com.habitTap.data.repository.CounterRepository
import com.habitTap.model.Background
import com.habitTap.model.Counter
import com.habitTap.model.CounterType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * FR-8.6, and TT-031 decision 4 (a slip still gets a message, worded for the restart).
 *
 * Driven through `TestListenableWorkerBuilder` rather than the real job scheduler, so the
 * assertions are about what the worker actually posts.
 */
@RunWith(RobolectricTestRunner::class)
class DailyMessageWorkerTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CounterRepository
    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * The worker reads the system clock, because it really does run at 09:00. The
     * fixtures therefore hang off the same clock rather than a frozen constant — with a
     * fake one, "14 days ago" would be hundreds of days ago as the worker sees it.
     */
    private val now = System.currentTimeMillis()
    private val day = 24L * 60 * 60 * 1000

    @Before
    fun setUp() {
        // The worker arms the next morning before it returns, so WorkManager has to
        // exist. SynchronousExecutor keeps that enqueue on this thread, so the test can
        // read back what was scheduled.
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setMinimumLoggingLevel(Log.ERROR)
                .build(),
        )
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        repository = CounterRepository(db.counterDao()) { now }
        startKoin { modules(module { single { repository } }) }
        // Robolectric denies runtime permissions by default; the worker checks for
        // POST_NOTIFICATIONS and does nothing without it (see the last test).
        shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @After
    fun tearDown() {
        stopKoin()
        db.close()
    }

    private fun quit(
        name: String,
        daysAgo: Int,
        dailyMessageOn: Boolean = true,
        hoursAgo: Int = 0,
    ) = Counter(
        id = 0, type = CounterType.QUIT, name = name, notes = null, color = null,
        background = Background.NONE, isPinned = false, isFocus = false, createdAt = now,
        sortOrder = 0, count = 0, startValue = 0, step = 1, target = null,
        keepCountingAfterTarget = false, isLocked = false, isReversed = false,
        quitAt = now - daysAgo * day - hoursAgo * 60 * 60 * 1000,
        moneyPerDay = null, goalDays = null, reason = null, bestStreakDays = 0,
        badgesOn = true, dailyMessageOn = dailyMessageOn, dailyMessageMinute = 540,
    )

    private fun runWorker(): ListenableWorker.Result {
        val worker = TestListenableWorkerBuilder<DailyMessageWorker>(context).build()
        return kotlinx.coroutines.runBlocking { worker.doWork() }
    }

    private fun posted() =
        shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications

    @Test
    fun `one notification per quit counter with the option on`() = runTest {
        repository.create(quit("Quit cigarettes", daysAgo = 14))
        repository.create(quit("Quit soft drinks", daysAgo = 3))
        repository.create(quit("Silent one", daysAgo = 5, dailyMessageOn = false))

        assertEquals(ListenableWorker.Result.success(), runWorker())
        assertEquals("only the two opted-in counters", 2, posted().size)
    }

    @Test
    fun `the message carries days free and the next badge`() = runTest {
        repository.create(quit("Quit cigarettes", daysAgo = 14))
        runWorker()

        val n = posted().single()
        val title = n.extras.getString("android.title").orEmpty()
        val text = n.extras.getString("android.text").orEmpty()

        assertEquals("Quit cigarettes", title)
        assertTrue("says days free, was: $text", text.contains("14 days free"))
        assertTrue("names the next badge, was: $text", text.contains("21-day badge"))
    }

    @Test
    fun `TT-031 a counter that restarted today still gets a message, worded for the restart`() = runTest {
        repository.create(quit("Quit cigarettes", daysAgo = 0, hoursAgo = 2))
        runWorker()

        val n = posted().single()
        val title = n.extras.getString("android.title").orEmpty()
        val text = n.extras.getString("android.text").orEmpty()

        assertEquals("Day one again", title)
        assertTrue("acknowledges what is kept, was: $text", text.contains("kept"))
        assertTrue("never claims a streak, was: $text", !text.contains("days free"))
    }

    @Test
    fun `a counter past every badge is told so`() = runTest {
        repository.create(quit("Quit cigarettes", daysAgo = 400))
        runWorker()
        val text = posted().single().extras.getString("android.text").orEmpty()
        assertTrue("was: $text", text.contains("every badge"))
    }

    @Test
    fun `without notification permission the worker posts nothing and still succeeds`() = runTest {
        shadowOf(context as Application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        repository.create(quit("Quit cigarettes", daysAgo = 14))

        // Success, not failure: retrying forever would not make the user grant it.
        assertEquals(ListenableWorker.Result.success(), runWorker())
        assertEquals(0, posted().size)
    }

    @Test
    fun `normal counters never get a message`() = runTest {
        repository.create(
            Counter(
                id = 0, type = CounterType.NORMAL, name = "Push-ups", notes = null, color = null,
                background = Background.NONE, isPinned = false, isFocus = false, createdAt = now,
                sortOrder = 0, count = 0, startValue = 0, step = 1, target = null,
                keepCountingAfterTarget = false, isLocked = false, isReversed = false,
                quitAt = null, moneyPerDay = null, goalDays = null, reason = null,
                bestStreakDays = 0, badgesOn = true, dailyMessageOn = true, dailyMessageMinute = 540,
            ),
        )
        runWorker()
        assertEquals("reminders for normal counters are out of scope for v1", 0, posted().size)
    }

    /**
     * The chain has to re-arm itself, or the message is a one-off.
     *
     * This is what a periodic request used to do badly and what a one-time request does
     * not do at all unless the worker asks for the next one. If this fails, the user gets
     * exactly one morning message, ever.
     */
    @Test
    fun `the worker arms tomorrow's run before it finishes`() = runTest {
        repository.create(quit("Quit cigarettes", daysAgo = 3))

        runWorker()

        val scheduled = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(DailyMessageWorker.WORK_NAME)
            .get()
        assertEquals("one pending run, never a pile of them", 1, scheduled.size)
        assertEquals(WorkInfo.State.ENQUEUED, scheduled.first().state)
    }

    @Test
    fun `tomorrow is armed even when permission was refused`() = runTest {
        shadowOf(context as Application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        repository.create(quit("Quit cigarettes", daysAgo = 3))

        runWorker()

        val scheduled = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(DailyMessageWorker.WORK_NAME)
            .get()
        assertEquals(
            "permission can be granted tomorrow; a chain that stopped would never notice",
            1,
            scheduled.size,
        )
    }
}
