package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.local.entities.AppLimit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLimitsViewModel @Inject constructor(
    private val appLimitRepository: AppLimitRepository
) : ViewModel() {

    var newPackage by mutableStateOf("")
        private set

    var newLimitMinutes by mutableStateOf(30)
        private set

    val limits: StateFlow<List<AppLimit>> = appLimitRepository.getAllLimits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPackageChanged(value: String) { newPackage = value }

    fun onLimitChanged(value: Int) { newLimitMinutes = value }

    fun save() {
        viewModelScope.launch {
            if (newPackage.isNotBlank()) {
                appLimitRepository.upsert(
                    AppLimit(packageName = newPackage.trim(), appName = newPackage.trim(), dailyLimitMinutes = newLimitMinutes)
                )
                newPackage = ""
                newLimitMinutes = 30
            }
        }
    }

    fun delete(limit: AppLimit) {
        viewModelScope.launch { appLimitRepository.deleteById(limit.id) }
    }
}
