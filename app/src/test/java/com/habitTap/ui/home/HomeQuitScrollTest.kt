package com.habitTap.ui.home

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.habitTap.data.db.AppDatabase
import com.habitTap.data.repository.CounterRepository
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.data.settings.SettingsStore
import com.habitTap.model.Background
import com.habitTap.model.Counter
import com.habitTap.model.CounterType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * A new quit counter is shown wherever it landed.
 *
 * Quit counters sit below the normal ones, far enough down that a new one can be off
 * screen when Home comes back. Where Home goes depends on where the counter actually
 * went, not on what was ticked: a pinned one heads the quit list, a focus one has its own
 * card at the top of Home, and the rest join the end. A counter that was already there
 * when the app opened was not just made, and nothing moves for it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeQuitScrollTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var db: AppDatabase
    private lateinit var counters: CounterRepository
    private lateinit var settings: SettingsRepository

    private val now = 1_762_000_000_000L

    /**
     * Holds every ViewModel the test makes, so they can be cancelled at the end.
     *
     * Each one collects a ticker that never completes. Left running, those collectors
     * outlive the test and are still on the main dispatcher when the next test replaces
     * it — "Dispatchers.Main is used concurrently with setting it", from a test that had
     * already passed. [ViewModelStore.clear] is the public way to end viewModelScope.
     */
    private val viewModels = ViewModelStore()

    @Before
    fun setUp() {
        // The ViewModel collects on viewModelScope, which is the main dispatcher.
        // The real Unconfined, not a test dispatcher.
        //
        // These tests drive real Room queries and wait for them in real time, while
        // `runTest` runs the test body on a scheduler of its own. A test dispatcher
        // installed as Main would hold the ViewModel's continuations in a second
        // scheduler that nothing drains while the body waits, so an effect could simply
        // never arrive. Unconfined resumes them inline on whichever thread Room used.
        Dispatchers.setMain(Dispatchers.Unconfined)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        counters = CounterRepository(db.counterDao()) { now }
        settings = SettingsRepository(
            SettingsStore(
                PreferenceDataStoreFactory.create {
                    tempFolder.newFile("home.preferences_pb").also { it.delete() }
                },
            ),
        )
    }

    @After
    fun tearDown() {
        // Before resetMain, so nothing is still using the dispatcher as it goes away.
        viewModels.clear()
        if (::db.isInitialized) db.close()
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(counters, settings) { now }
        .also { viewModels.put(it.toString(), it) }

    private suspend fun addQuit(
        name: String,
        pinned: Boolean = false,
        focus: Boolean = false,
    ) = counters.create(
        Counter(
            id = 0, type = CounterType.QUIT, name = name, notes = null, color = null,
            background = Background.NONE, isPinned = pinned, isFocus = focus,
            createdAt = now, sortOrder = 0, count = 0, startValue = 0, step = 1,
            target = null, keepCountingAfterTarget = false, isLocked = false,
            isReversed = false, quitAt = now, moneyPerDay = null, goalDays = null,
            reason = null, bestStreakDays = 0, badgesOn = true, dailyMessageOn = true,
            dailyMessageMinute = 540,
        ),
    )

    /**
     * The next effect, or null if none arrives — the tests care about both answers.
     *
     * Waited for on [Dispatchers.Default], not the test scheduler: Room emits from its own
     * executor in real time, and `runTest` would fast-forward the timeout past it.
     */
    private suspend fun HomeViewModel.nextEffect(): HomeEffect? =
        withContext(Dispatchers.Default) {
            withTimeoutOrNull(1_000) { effects.first() }
        }

    @Test
    fun `a new quit counter scrolls Home to its end`() = runTest {
        val viewModel = viewModel()
        // Home has loaded and knows what is already there.
        viewModel.state.first { !it.isLoading }

        addQuit("Quit cigarettes")

        assertEquals(HomeEffect.ScrollToEnd, viewModel.nextEffect())
    }

    @Test
    fun `every new quit counter scrolls, not only the first`() = runTest {
        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading }
        addQuit("Quit cigarettes")
        assertEquals(HomeEffect.ScrollToEnd, viewModel.nextEffect())

        addQuit("Quit soft drinks")

        assertEquals(HomeEffect.ScrollToEnd, viewModel.nextEffect())
    }

    @Test
    fun `a pinned one shows the top of the quit list instead`() = runTest {
        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading }

        // Pinned to the top, so the end of the page is the wrong place to look.
        addQuit("Quit cigarettes", pinned = true)

        assertEquals(HomeEffect.ScrollToQuitTop, viewModel.nextEffect())
    }

    @Test
    fun `a focus one shows the top of Home, pinned or not`() = runTest {
        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading }

        // The focus card is at the top of Home and leaves the quit list entirely, so it
        // wins over the pin.
        addQuit("Quit cigarettes", pinned = true, focus = true)

        assertEquals(HomeEffect.ScrollToTop, viewModel.nextEffect())
    }

    @Test
    fun `a quit counter that was already there on open does not move the page`() = runTest {
        addQuit("Quit cigarettes")

        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading && it.allQuitCounters.isNotEmpty() }

        assertNull("nothing was just made", viewModel.nextEffect())
    }

    @Test
    fun `pinning a counter that already exists does not move the page`() = runTest {
        val id = addQuit("Quit cigarettes")
        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading && it.allQuitCounters.isNotEmpty() }

        counters.togglePin(id)
        viewModel.state.first { it.allQuitCounters.any { q -> q.isPinned } }

        assertNull("the scroll is for new counters, not for pinning", viewModel.nextEffect())
    }

    @Test
    fun `a normal counter never moves the page`() = runTest {
        val viewModel = viewModel()
        viewModel.state.first { !it.isLoading }

        counters.create(
            Counter(
                id = 0, type = CounterType.NORMAL, name = "Push-ups", notes = null,
                color = null, background = Background.NONE, isPinned = false,
                isFocus = false, createdAt = now, sortOrder = 0, count = 0,
                startValue = 0, step = 1, target = null, keepCountingAfterTarget = false,
                isLocked = false, isReversed = false, quitAt = null, moneyPerDay = null,
                goalDays = null, reason = null, bestStreakDays = 0, badgesOn = true,
                dailyMessageOn = true, dailyMessageMinute = 540,
            ),
        )
        viewModel.state.first { it.normalCounters.isNotEmpty() }

        assertNull(viewModel.nextEffect())
    }
}
