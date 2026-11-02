package com.taptap.di

import android.content.Context
import com.taptap.ui.counter.CounterViewModel
import com.taptap.ui.edit.EditViewModel
import com.taptap.ui.quit.QuitViewModel
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

/**
 * Catches a missing or mis-ordered definition here instead of at app launch. The whole
 * graph lives in one file (tech spec 6), so one test covers it.
 */
class AppModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every definition in the app module can be resolved`() {
        appModule.verify(
            // Supplied by androidContext() at start-up, so it has no definition of its own.
            extraTypes = listOf(Context::class),
            injections = injectedParameters(
                // These three take a counter id from their navigation key at call time.
                definition<CounterViewModel>(Long::class),
                definition<QuitViewModel>(Long::class),
                definition<EditViewModel>(Long::class),
            ),
        )
    }
}
