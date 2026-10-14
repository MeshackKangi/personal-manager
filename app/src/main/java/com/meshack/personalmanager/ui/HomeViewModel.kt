package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.*
import com.meshack.personalmanager.data.settings.SettingsRepository
import com.meshack.personalmanager.domain.DayType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineBlockRepository: RoutineBlockRepository,
    private val taskRepository: TaskRepository,
    private val recitalRepository: RecitalRepository,
    private val appLimitRepository: AppLimitRepository,
    private val usageLogRepository: UsageLogRepository,
    private val goalRepository: GoalRepository,
    private val pomodoroRepository: PomodoroRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    var dashboard by mutableStateOf(Dashboard())
        private set

    var showActiveBlock by mutableStateOf("No active block")
        private set

    var nextBlockName by mutableStateOf("—")
        private set

    var completionPercent by mutableStateOf(0f)
        private set

    val welcomeName: StateFlow<String> = settingsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val congratsMessage: StateFlow<String> = settingsRepository.congratsMessage
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            "Goal complete — congratulations! Take a moment to be proud."
        )

    val goalSummaries: StateFlow<List<GoalSummary>> = goalRepository.getSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<com.meshack.personalmanager.data.local.entities.Task>> =
        taskRepository.getTasksFor(LocalDate.now())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklistedApps: StateFlow<List<com.meshack.personalmanager.data.local.entities.AppLimit>> =
        appLimitRepository.getAllLimits()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusTargetMinutes: StateFlow<Int> = settingsRepository.focusTargetMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 240)

    val focusStats: StateFlow<FocusStats> =
        pomodoroRepository.sessionsSince(LocalDate.now().minusDays(365))
            .map { sessions ->
                val today = LocalDate.now()
                FocusStats(
                    todayMinutes = sessions.filter { it.date == today }.sumOf { it.focusMinutesTotal },
                    totalMinutes = sessions.sumOf { it.focusMinutesTotal }
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusStats())

    /** Current + next routine block, refreshed every minute. */
    val routineOverview: StateFlow<RoutineOverview> = flow {
        while (true) {
            val today = LocalDate.now()
            val dayType = if (today.dayOfWeek.value >= 6) DayType.WEEKEND else DayType.WEEKDAY
            val now = java.time.LocalTime.now()
            val minute = now.hour * 60 + now.minute
            val current = routineBlockRepository.getCurrentActiveBlock(today)
            val next = routineBlockRepository.getNextBlockAfter(minute, dayType)
                ?.takeIf { it.id != current?.id }
            emit(RoutineOverview(current, next))
            delay(60_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutineOverview(null, null))

    init {
        viewModelScope.launch {
            combine(
                routineBlockRepository.getBlocksFor(DayType.WEEKDAY),
                taskRepository.getTasksFor(LocalDate.now()),
                recitalRepository.getAllRecitals(),
                routineBlockRepository.getBlocksFor(DayType.WEEKEND)
            ) { weekdayBlocks, tasks, recitals, _ ->
                val total = tasks.size
                val done = tasks.count { it.done }
                val now = java.time.LocalDateTime.now()
                val minute = now.hour * 60 + now.minute
                val active = weekdayBlocks.firstOrNull { it.startMinute <= minute && it.endMinute > minute }
                val upcoming = weekdayBlocks.firstOrNull { it.startMinute > minute }
                dashboard = Dashboard(
                    activeBlock = active?.name ?: "No active block",
                    nextBlockName = upcoming?.name ?: "—",
                    completionPercent = if (total > 0) done.toFloat() / total else 0f,
                    tasksToday = total,
                    tasksDone = done,
                    recitalsCount = recitals.size
                )
                showActiveBlock = active?.name ?: "No active block"
                nextBlockName = upcoming?.name ?: "—"
                completionPercent = if (total > 0) done.toFloat() / total else 0f
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Dashboard())
        }
    }

    data class Dashboard(
        val activeBlock: String = "—",
        val nextBlockName: String = "—",
        val completionPercent: Float = 0f,
        val tasksToday: Int = 0,
        val tasksDone: Int = 0,
        val recitalsCount: Int = 0
    )

    data class FocusStats(
        val todayMinutes: Int = 0,
        val totalMinutes: Int = 0
    )

    fun toggleTask(task: com.meshack.personalmanager.data.local.entities.Task) {
        viewModelScope.launch { taskRepository.toggleDone(task) }
    }

    data class RoutineOverview(
        val current: com.meshack.personalmanager.data.local.entities.RoutineBlock? = null,
        val next: com.meshack.personalmanager.data.local.entities.RoutineBlock? = null
    )
}
