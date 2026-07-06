package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.*
import com.meshack.personalmanager.domain.DayType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val usageLogRepository: UsageLogRepository
) : ViewModel() {

    var dashboard by mutableStateOf(Dashboard())
        private set

    var showActiveBlock by mutableStateOf("No active block")
        private set

    var nextBlockName by mutableStateOf("—")
        private set

    var completionPercent by mutableStateOf(0f)
        private set

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
                val dayType = if (now.dayOfWeek.value >= 6) DayType.WEEKEND else DayType.WEEKDAY
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
}
