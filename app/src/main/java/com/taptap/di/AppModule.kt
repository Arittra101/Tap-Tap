package com.taptap.di

import androidx.room.Room
import com.taptap.data.db.AppDatabase
import com.taptap.data.repository.CounterRepository
import com.taptap.data.repository.SettingsRepository
import com.taptap.data.settings.SettingsStore
import com.taptap.ui.counter.CounterViewModel
import com.taptap.ui.create.CreateViewModel
import com.taptap.ui.edit.EditViewModel
import com.taptap.ui.home.HomeViewModel
import com.taptap.ui.quit.QuitViewModel
import com.taptap.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The whole app in one file, readable top to bottom (tech spec 6).
 *
 * Constructor injection only; repositories are concrete classes with no interface until
 * a second implementation actually exists.
 */
val appModule = module {

    // --- Data ---
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME).build()
    }
    single { get<AppDatabase>().counterDao() }
    single { SettingsStore.create(androidContext()) }
    single { SettingsStore(get()) }

    // --- Repositories ---
    single { CounterRepository(get()) }
    single { SettingsRepository(get()) }

    // --- ViewModels ---
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CreateViewModel(get()) }
    viewModel { (counterId: Long) -> CounterViewModel(counterId, get()) }
    viewModel { (counterId: Long) -> QuitViewModel(counterId, get()) }
    viewModel { (counterId: Long) -> EditViewModel(counterId, get()) }
    viewModel { SettingsViewModel(get()) }
}
