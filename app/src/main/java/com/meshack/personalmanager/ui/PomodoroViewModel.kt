package com.meshack.personalmanager.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.PomodoroRepository
import com.meshack.personalmanager.services.FocusPhase
import com.meshack.personalmanager.services.FocusService
import com.meshack.personalmanager.services.FocusState
import com.meshack.personalmanager.services.FocusStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val appLimitRepository: AppLimitRepository,
    private val focusState: FocusStateHolder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var isRunning by mutableStateOf(false)
        private set

    var remainingSeconds by mutableStateOf(FocusState.WORK_SECONDS)
        private set

    var phase by mutableStateOf(FocusPhase.WORK)
        private set

    var completedCycles by mutableStateOf(0)
        private set

    var showNotification by mutableStateOf(false)
        private set

    val history: StateFlow<List<com.meshack.personalmanager.data.local.entities.PomodoroSession>> =
        pomodoroRepository.sessionsSince(LocalDate.now().minusDays(30))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            focusState.state.collect { state ->
                isRunning = state.isRunning
                remainingSeconds = state.remainingSeconds
                phase = state.phase
                completedCycles = state.completedCycles
                showNotification = state.isRunning
            }
        }
    }

    fun toggle() {
        if (isRunning) {
            context.startService(FocusService.pauseIntent(context))
        } else {
            ContextCompat.startForegroundService(context, FocusService.startIntent(context))
        }
    }

    /** Resets the current phase's clock (WORK 25:00, BREAK 5:00, LONG_BREAK 15:00). */
    fun reset() {
        context.startService(FocusService.resetIntent(context))
    }

    fun resetWorkPhase() = reset()

    fun resetBreakPhase() = reset()

    fun start() {
        ContextCompat.startForegroundService(context, FocusService.startIntent(context))
    }

    fun stop() {
        context.startService(FocusService.pauseIntent(context))
    }
}
