package com.habitTap.ui.quit

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.habitTap.data.db.AppDatabase
import com.habitTap.data.repository.CounterRepository
import com.habitTap.model.Background
import com.habitTap.model.Counter
import com.habitTap.model.CounterType
import com.habitTap.model.ThemeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * A quit counter looks the way Edit says it should.
 *
 * Edit offers the background and colour pickers for every counter and saves what it is
 * given, so a field that the quit screen's state forgets to carry is stored, invisible,
 * and impossible to tell apart from a picker that does not work — which is how the
 * background came to be chosen and never drawn.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuitAppearanceTest {

    private lateinit var db: AppDatabase
    private lateinit var counters: CounterRepository

    private val now = 1_762_000_000_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        counters = CounterRepository(db.counterDao()) { now }
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private suspend fun quitCounter(
        background: Background = Background.NONE,
        color: ThemeColor? = null,
    ): Long = counters.create(
        Counter(
            id = 0, type = CounterType.QUIT, name = "Quit cigarettes", notes = null,
            color = color, background = background, isPinned = false, isFocus = false,
            createdAt = now, sortOrder = 0, count = 0, startValue = 0, step = 1,
            target = null, keepCountingAfterTarget = false, isLocked = false,
            isReversed = false, quitAt = now, moneyPerDay = null, goalDays = null,
            reason = null, bestStreakDays = 0, badgesOn = true, dailyMessageOn = true,
            dailyMessageMinute = 540,
        ),
    )

    private suspend fun loadedState(id: Long): QuitState =
        QuitViewModel(id, counters) { now }.state.first { !it.isLoading }

    @Test
    fun `the chosen background reaches the screen`() = runTest {
        val id = quitCounter(background = Background.WAVES)

        assertEquals(Background.WAVES, loadedState(id).background)
    }

    @Test
    fun `every background the picker offers reaches the screen`() = runTest {
        Background.entries.forEach { background ->
            val id = quitCounter(background = background)
            assertEquals("$background was not carried", background, loadedState(id).background)
        }
    }

    @Test
    fun `the chosen colour reaches the screen`() = runTest {
        val id = quitCounter(color = ThemeColor.TEAL)

        assertEquals(ThemeColor.TEAL, loadedState(id).color)
    }

    @Test
    fun `a counter with no background of its own has none`() = runTest {
        val id = quitCounter()

        assertEquals(Background.NONE, loadedState(id).background)
    }
}
