package com.example.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val apiKey = repository.apiKeyFlow.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val themeMode = repository.themeModeFlow.stateIn(viewModelScope, SharingStarted.Lazily, "system")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            repository.saveApiKey(key)
        }
    }

    fun saveThemeMode(mode: String) { // "light", "dark", "system"
        viewModelScope.launch {
            repository.saveThemeMode(mode)
        }
    }
}
