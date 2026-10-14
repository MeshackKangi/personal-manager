package com.meshack.personalmanager.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class FocusPhase { WORK, BREAK, LONG_BREAK }

/**
 * Single source of truth for the focus timer. Written by [FocusService]
 * (which survives the app going to background), read by the UI layer.
 */
data class FocusState(
    val phase: FocusPhase = FocusPhase.WORK,
    val remainingSeconds: Int = WORK_SECONDS,
    val isRunning: Boolean = false,
    val completedCycles: Int = 0
) {
    companion object {
        const val WORK_SECONDS = 25 * 60
        const val BREAK_SECONDS = 5 * 60
        const val LONG_BREAK_SECONDS = 15 * 60
    }
}

@Singleton
class FocusStateHolder @Inject constructor() {
    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    fun update(transform: (FocusState) -> FocusState) {
        _state.value = transform(_state.value)
    }
}
