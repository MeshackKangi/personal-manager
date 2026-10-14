package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.GoalDao
import com.meshack.personalmanager.data.local.daos.MilestoneDao
import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.entities.Goal
import com.meshack.personalmanager.data.local.entities.Milestone
import com.meshack.personalmanager.data.local.entities.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

data class GoalSummary(
    val goal: Goal,
    val percent: Float,
    val doneTasks: Int,
    val totalTasks: Int
)

class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val milestoneDao: MilestoneDao,
    private val taskDao: TaskDao
) {
    fun getOpenGoals(): Flow<List<Goal>> = goalDao.getOpenGoals()

    fun getArchivedGoals(): Flow<List<Goal>> = goalDao.getArchivedGoals()

    /** Milestone id → parent goal title, for chips in shared lists. */
    fun milestoneGoalTitles(): Flow<Map<Long, String>> = combine(
        goalDao.getOpenGoals(),
        milestoneDao.getAll()
    ) { goals, milestones ->
        val titles = goals.associate { it.id to it.title }
        milestones.mapNotNull { m -> titles[m.goalId]?.let { m.id to it } }.toMap()
    }

    fun getMilestones(goalId: Long): Flow<List<Milestone>> = milestoneDao.getByGoal(goalId)

    /** Goals with rolled-up progress, most recently worked-on first. */
    fun getSummaries(): Flow<List<GoalSummary>> = combine(
        goalDao.getOpenGoals(),
        milestoneDao.getAll(),
        taskDao.getAll()
    ) { goals, milestones, tasks ->
        val byGoal = milestones.groupBy { it.goalId }
        val byMilestone = tasks.groupBy { it.milestoneId }
        goals.map { goal ->
            val ms = byGoal[goal.id].orEmpty()
            val all = ms.flatMap { m -> byMilestone[m.id].orEmpty() }
            val total = all.size
            val done = all.count { it.done }
            val percent = if (ms.isEmpty() || total == 0) 0f else done.toFloat() / total.toFloat()
            GoalSummary(goal, percent.coerceIn(0f, 1f), done, total)
        }.sortedByDescending { it.goal.updatedAt }
    }

    suspend fun insertGoal(goal: Goal): Long = goalDao.insert(goal)

    suspend fun insertMilestone(milestone: Milestone): Long = milestoneDao.insert(milestone)

    suspend fun renameGoal(id: Long, title: String) {
        val goal = goalDao.getById(id) ?: return
        goalDao.update(goal.copy(title = title, updatedAt = LocalDateTime.now()))
    }

    suspend fun archiveGoal(id: Long) =
        goalDao.archive(id, LocalDateTime.now())

    suspend fun unarchiveGoal(id: Long) =
        goalDao.unarchive(id, LocalDateTime.now())

    suspend fun updateMilestone(milestone: Milestone) {
        milestoneDao.update(milestone.copy(updatedAt = LocalDateTime.now()))
        touchGoal(milestone.goalId)
    }

    /** Deletes a milestone plus all its tasks and their subtasks. */
    suspend fun deleteMilestoneCascade(milestoneId: Long) {
        val milestone = milestoneDao.getById(milestoneId) ?: return
        val snapshot = taskDao.getByMilestone(milestoneId).first()
        snapshot.forEach { task ->
            taskDao.deleteSubtasks(task.id)
            taskDao.delete(task)
        }
        milestoneDao.deleteById(milestoneId)
        touchGoal(milestone.goalId)
    }

    /** Deletes a task plus its subtasks. */
    suspend fun deleteTaskCascade(task: Task) {
        taskDao.deleteSubtasks(task.id)
        taskDao.delete(task)
        task.milestoneId?.let { touchMilestone(it) }
    }

    suspend fun touchGoal(id: Long) =
        goalDao.touchUpdated(id, LocalDateTime.now())

    suspend fun touchMilestone(id: Long) =
        milestoneDao.touchUpdated(id, LocalDateTime.now())
}
