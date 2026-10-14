package com.meshack.personalmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userName: StateFlow<String> = settingsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val use24h: StateFlow<Boolean> = settingsRepository.use24h
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val focusTargetMinutes: StateFlow<Int> = settingsRepository.focusTargetMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 240)

    val congratsMessage: StateFlow<String> = settingsRepository.congratsMessage
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            "Goal complete — congratulations! Take a moment to be proud."
        )

    fun onUserNameChanged(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name) }
    }

    fun onUse24hChanged(use24h: Boolean) {
        viewModelScope.launch { settingsRepository.setUse24h(use24h) }
    }

    fun onFocusTargetChanged(minutes: Int) {
        viewModelScope.launch { settingsRepository.setFocusTargetMinutes(minutes) }
    }

    fun onCongratsMessageChanged(message: String) {
        viewModelScope.launch { settingsRepository.setCongratsMessage(message) }
    }
}
