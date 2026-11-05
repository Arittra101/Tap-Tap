package com.habitTap

import android.app.Application
import com.habitTap.di.appModule
import com.habitTap.util.DailyMessageWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/** Application entry point. */
class TapTapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@TapTapApp)
            modules(appModule)
        }

        // FR-8.6: one notification per quit counter per day. UPDATE keeps a relaunch from
        // stacking a second worker.
        DailyMessageWorker.schedule(this)
    }
}
