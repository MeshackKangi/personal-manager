package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.GoalRepository
import com.meshack.personalmanager.data.repositories.TaskRepository
import com.meshack.personalmanager.data.local.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    goalRepository: GoalRepository
) : ViewModel() {

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    var showNewTaskDialog by mutableStateOf(false)
        private set

    var newTaskTitle by mutableStateOf("")
        private set

    var completionPercent by mutableStateOf(0f)
        private set

    var tasks: StateFlow<List<Task>> = taskRepository.getTasksFor(selectedDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalTitles: StateFlow<Map<Long, String>> = goalRepository.milestoneGoalTitles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init { recalc() }

    fun onDateChanged(date: LocalDate) {
        selectedDate = date
        viewModelScope.launch {
            tasks = taskRepository.getTasksFor(date)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            recalc()
        }
    }

    fun openNewTaskDialog() { showNewTaskDialog = true }

    fun dismissNewTaskDialog() { showNewTaskDialog = false; newTaskTitle = "" }

    fun addTask(title: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                taskRepository.insert(
                    Task(
                        title = title.trim(),
                        forDate = selectedDate
                    )
                )
                newTaskTitle = ""
                showNewTaskDialog = false
                recalc()
            }
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleDone(task)
            recalc()
        }
    }

    private fun recalc() {
        viewModelScope.launch { completionPercent = taskRepository.completionPercent(selectedDate) }
    }
}
