package com.habitTap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitTap.data.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** TT-025 fills in the screen; the wiring below is already final. */
class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { s ->
                _state.update {
                    it.copy(
                        color = s.themeColor,
                        mode = s.themeMode,
                        languageTag = s.languageTag.ifEmpty { SettingsRepository.FALLBACK_LANGUAGE },
                        vibration = s.vibration,
                        sound = s.sound,
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ChangeColor -> viewModelScope.launch { repository.setThemeColor(intent.color) }
            is SettingsIntent.ChangeMode -> viewModelScope.launch { repository.setThemeMode(intent.mode) }
            is SettingsIntent.ChangeLanguage -> viewModelScope.launch {
                repository.setLanguageTag(intent.tag)
                _effects.send(SettingsEffect.ApplyLanguage(intent.tag))
            }
            is SettingsIntent.ToggleVibration -> viewModelScope.launch { repository.setVibration(intent.on) }
            is SettingsIntent.ToggleSound -> viewModelScope.launch { repository.setSound(intent.on) }
            SettingsIntent.OpenPrivacyPolicy -> viewModelScope.launch {
                _effects.send(SettingsEffect.OpenPrivacyPolicy)
            }
        }
    }
}
