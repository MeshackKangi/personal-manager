package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.PomodoroRepository
import com.meshack.personalmanager.data.repositories.TaskRepository
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val appLimitRepository: AppLimitRepository
) : ViewModel() {

    var isRunning by mutableStateOf(false)
        private set

    var remainingSeconds by mutableStateOf(25 * 60)
        private set

    var phase by mutableStateOf(Phase.WORK)
        private set

    var completedCycles by mutableStateOf(0)
        private set

    var showNotification by mutableStateOf(false)
        private set

    private var todaySession: PomodoroSession? = null
    private var workSecondsTotal: Int = 0

    val history: StateFlow<List<PomodoroSession>> = pomodoroRepository.sessionsSince(LocalDate.now().minusDays(30))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { ensureSession() }

    fun toggle() {
        if (isRunning) stop() else start()
    }

    fun resetWorkPhase() {
        remainingSeconds = 25 * 60
        phase = Phase.WORK
        showNotification = false
    }

    fun resetBreakPhase() {
        remainingSeconds = 5 * 60
        phase = Phase.BREAK
        showNotification = false
    }

    fun start() {
        if (todaySession == null) ensureSession()
        isRunning = true
        viewModelScope.launch {
            while (isActive && isRunning) {
                delay(1000L)
                if (remainingSeconds > 0) {
                    if (phase == Phase.WORK) workSecondsTotal++
                    remainingSeconds--
                } else {
                    when (phase) {
                        Phase.WORK -> {
                            completedCycles++
                            saveSession()
                            if (completedCycles >= 4) {
                                phase = Phase.LONG_BREAK
                                remainingSeconds = 15 * 60
                            } else {
                                phase = Phase.BREAK
                                remainingSeconds = 5 * 60
                            }
                        }
                        Phase.BREAK -> {
                            phase = Phase.WORK
                            remainingSeconds = 25 * 60
                        }
                        Phase.LONG_BREAK -> {
                            completedCycles = 0
                            phase = Phase.WORK
                            remainingSeconds = 25 * 60
                        }
                    }
                    showNotification = true
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        viewModelScope.launch { saveSession() }
    }

    private fun ensureSession() {
        todaySession = PomodoroSession(date = LocalDate.now(), startedAt = java.time.LocalDateTime.now())
        viewModelScope.launch { saveSession() }
    }

    private suspend fun saveSession() {
        todaySession?.let {
            pomodoroRepository.insert(
                it.copy(
                    completedCycles = completedCycles,
                    focusMinutesTotal = workSecondsTotal / 60
                )
            )
        }
    }

    enum class Phase { WORK, BREAK, LONG_BREAK }
}
