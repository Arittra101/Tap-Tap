package com.habitTap.di

import androidx.room.Room
import com.habitTap.data.db.AppDatabase
import com.habitTap.data.repository.CounterRepository
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.data.settings.SettingsStore
import com.habitTap.ui.counter.CounterViewModel
import com.habitTap.ui.create.CreateViewModel
import com.habitTap.ui.edit.EditViewModel
import com.habitTap.ui.home.HomeViewModel
import com.habitTap.ui.quit.QuitViewModel
import com.habitTap.ui.settings.SettingsViewModel
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
