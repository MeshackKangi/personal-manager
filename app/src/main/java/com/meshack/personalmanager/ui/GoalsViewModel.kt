package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.local.entities.Goal
import com.meshack.personalmanager.data.local.entities.Milestone
import com.meshack.personalmanager.data.local.entities.Task
import com.meshack.personalmanager.data.repositories.GoalRepository
import com.meshack.personalmanager.data.repositories.GoalSummary
import com.meshack.personalmanager.data.repositories.TaskRepository
import com.meshack.personalmanager.domain.MilestonePriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val summaries: StateFlow<List<GoalSummary>> = goalRepository.getSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var showArchived by mutableStateOf(false)
        private set

    val archived: StateFlow<List<Goal>> = goalRepository.getArchivedGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFilterChanged(archived: Boolean) {
        showArchived = archived
    }

    fun unarchiveGoal(id: Long) {
        viewModelScope.launch { goalRepository.unarchiveGoal(id) }
    }

    fun milestones(goalId: Long) = goalRepository.getMilestones(goalId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tasksFor(milestoneId: Long) = taskRepository.tasksForMilestone(milestoneId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun subtasksOf(parentId: Long): List<Task> =
        taskRepository.subtasksOf(parentId)

    fun createGoal(title: String, targetDate: LocalDate?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            goalRepository.insertGoal(Goal(title = title.trim(), targetDate = targetDate))
        }
    }

    fun renameGoal(id: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { goalRepository.renameGoal(id, title.trim()) }
    }

    fun archiveGoal(id: Long) {
        viewModelScope.launch { goalRepository.archiveGoal(id) }
    }

    fun createMilestone(
        goalId: Long,
        title: String,
        notes: String,
        priority: MilestonePriority
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            goalRepository.insertMilestone(
                Milestone(goalId = goalId, title = title.trim(), notes = notes.trim(), priority = priority)
            )
            goalRepository.touchGoal(goalId)
        }
    }

    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch { goalRepository.updateMilestone(milestone) }
    }

    fun deleteMilestone(milestoneId: Long) {
        viewModelScope.launch { goalRepository.deleteMilestoneCascade(milestoneId) }
    }

    fun addTask(milestone: Milestone, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.insert(
                Task(title = title.trim(), milestoneId = milestone.id, forDate = LocalDate.now())
            )
            goalRepository.touchMilestone(milestone.id)
            goalRepository.touchGoal(milestone.goalId)
        }
    }

    fun addSubtask(parent: Task, milestone: Milestone, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.insert(
                Task(
                    title = title.trim(),
                    milestoneId = milestone.id,
                    parentTaskId = parent.id,
                    forDate = parent.forDate
                )
            )
            goalRepository.touchMilestone(milestone.id)
            goalRepository.touchGoal(milestone.goalId)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch { taskRepository.toggleDone(task) }
    }

    fun renameTask(task: Task, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { taskRepository.renameTask(task.id, title.trim()) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { goalRepository.deleteTaskCascade(task) }
    }
}
