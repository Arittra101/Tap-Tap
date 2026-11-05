package com.habitTap.navigation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.habitTap.ui.counter.CounterRoute
import com.habitTap.ui.create.CreateRoute
import com.habitTap.ui.edit.EditRoute
import com.habitTap.ui.home.HomeRoute
import com.habitTap.ui.quit.QuitRoute
import com.habitTap.ui.settings.SettingsRoute
import com.habitTap.data.repository.SettingsRepository
import com.habitTap.util.rememberFeedback
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * The whole navigation graph.
 *
 * Navigation 3 treats the back stack as a list: opening a screen appends a key, going
 * back removes the last one. Only this file touches the stack — screens are handed
 * lambdas named for what the user did, not for where they go (tech spec 5).
 */
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeKey)
    // FR-8.4 / FR-8.5: one Feedback for the whole app, reading the live settings.
    val settingsRepository = koinInject<SettingsRepository>()
    val feedback = rememberFeedback(settingsRepository.settings)

    fun open(key: NavKey) {
        backStack.add(key)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Each screen gets its own ViewModel, scoped to its entry on the stack.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {

            entry<HomeKey> {
                HomeRoute(
                    viewModel = koinViewModel(),
                    onOpenCounter = { id -> open(CounterKey(id)) },
                    onOpenQuit = { id -> open(QuitKey(id)) },
                    onCreate = { quit -> open(CreateKey(startAsQuit = quit)) },
                    onSettings = { open(SettingsKey) },
                    onTapFeedback = feedback::tap,
                )
            }

            entry<CreateKey> { key ->
                CreateRoute(
                    viewModel = koinViewModel(),
                    startAsQuit = key.startAsQuit,
                    onBack = { goBack() },
                    onCreated = { goBack() },
                )
            }

            entry<CounterKey> { key ->
                CounterRoute(
                    viewModel = koinViewModel { parametersOf(key.counterId) },
                    onBack = { goBack() },
                    onEdit = { open(EditKey(key.counterId)) },
                    onTapFeedback = feedback::tap,
                    onSuccessFeedback = feedback::success,
                )
            }

            entry<QuitKey> { key ->
                QuitRoute(
                    viewModel = koinViewModel { parametersOf(key.counterId) },
                    onBack = { goBack() },
                    onEdit = { open(EditKey(key.counterId)) },
                    onUrgeFeedback = feedback::tap,
                )
            }

            entry<EditKey> { key ->
                EditRoute(
                    viewModel = koinViewModel { parametersOf(key.counterId) },
                    onBack = { goBack() },
                    onSaved = { goBack() },
                    onDeleted = {
                        // The counter this stack was built on is gone, so start over.
                        backStack.clear()
                        backStack.add(HomeKey)
                    },
                )
            }

            entry<SettingsKey> {
                SettingsRoute(
                    viewModel = koinViewModel(),
                    onBack = { goBack() },
                    // FR-8.2: a per-app locale, so the phone's language is untouched.
                    onApplyLanguage = { tag ->
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(tag),
                        )
                    },
                )
            }
        },
    )
}
