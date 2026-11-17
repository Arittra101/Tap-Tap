package com.habitTap.ui.journey

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.data.settings.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The journey's steps only ever move forward, and only from the step they belong to.
 *
 * That guard is what lets the app report what the user did from wherever it happens —
 * navigation, a form, a tap on a card — without any caller having to know whether a
 * journey is running or which step it is on.
 */
@RunWith(RobolectricTestRunner::class)
class JourneyControllerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private var counter = 0

    private fun settings(): SettingsRepository {
        val dataStore = PreferenceDataStoreFactory.create {
            tempFolder.newFile("journey-${counter++}.preferences_pb").also { it.delete() }
        }
        return SettingsRepository(SettingsStore(dataStore))
    }

    private fun controller() = JourneyController(settings())

    @Test
    fun `nothing runs until it is started`() {
        val journey = controller()
        assertNull(journey.step.value)
        // Facts reported while no journey runs change nothing.
        journey.onCreateFormOpened()
        journey.onNamed()
        journey.onCounterCreated()
        assertNull(journey.step.value)
    }

    @Test
    fun `the whole journey walks from the first step to done`() = runTest {
        val journey = controller()
        journey.startIfNeeded()
        assertEquals(JourneyStep.CREATE_COUNTER, journey.step.value)

        journey.onCreateFormOpened()
        assertEquals(JourneyStep.NAME_IT, journey.step.value)

        journey.onNamed()
        assertEquals(JourneyStep.TAP_CREATE, journey.step.value)

        journey.onCounterCreated()
        assertEquals(JourneyStep.FIRST_TAP, journey.step.value)

        journey.onCounterTapped()
        assertNull("finishing the last step ends the journey", journey.step.value)
    }

    @Test
    fun `creating straight from the name step skips ahead`() = runTest {
        val journey = controller()
        journey.startIfNeeded()
        journey.onCreateFormOpened()
        // Typed a name and pressed Create without pausing on the step in between.
        journey.onCounterCreated()
        assertEquals(JourneyStep.FIRST_TAP, journey.step.value)
    }

    @Test
    fun `a fact from the wrong step is ignored`() = runTest {
        val journey = controller()
        journey.startIfNeeded()
        journey.onNamed()
        assertEquals(
            "naming cannot advance a journey still waiting on the form being opened",
            JourneyStep.CREATE_COUNTER,
            journey.step.value,
        )
        journey.onCounterTapped()
        assertEquals(JourneyStep.CREATE_COUNTER, journey.step.value)
    }

    @Test
    fun `stopping is remembered, so it never starts again`() = runTest {
        val settings = settings()
        val journey = JourneyController(settings)
        journey.startIfNeeded()
        journey.stop()
        assertNull(journey.step.value)

        // The flag is written in the background, so wait for it before asking again.
        awaitJourneyDone(settings)
        JourneyController(settings).let {
            it.startIfNeeded()
            assertNull("a stopped journey stays stopped", it.step.value)
        }
    }

    @Test
    fun `starting twice leaves the user on the step they were on`() = runTest {
        val journey = controller()
        journey.startIfNeeded()
        journey.onCreateFormOpened()
        journey.startIfNeeded()
        assertEquals(JourneyStep.NAME_IT, journey.step.value)
    }

    /**
     * Waits for the background write.
     *
     * On [Dispatchers.Default] rather than the test scheduler: `runTest` fast-forwards its
     * own virtual time, so a delay on it would spin through the whole wait before the real
     * write had a chance to land.
     */
    private suspend fun awaitJourneyDone(settings: SettingsRepository) =
        withContext(Dispatchers.Default) {
            withTimeout(2_000) {
                while (!settings.isJourneyDone()) delay(10)
            }
        }
}
