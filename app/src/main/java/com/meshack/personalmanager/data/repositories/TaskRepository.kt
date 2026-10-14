package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.MilestoneDao
import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.entities.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val milestoneDao: MilestoneDao
) {
    fun getTasksFor(date: LocalDate): Flow<List<Task>> = dao.getTasksForDate(date)
    fun getTasksForBlock(date: LocalDate, blockId: Long?): Flow<List<Task>> = dao.getTasksForBlock(date, blockId)
    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun markDone(id: Long, done: Boolean) = dao.markDone(id, done)
    suspend fun toggleDone(task: Task) {
        dao.markDone(task.id, !task.done)
        // Mark the parent milestone/goal as recently worked-on.
        val milestoneId = task.milestoneId
            ?: dao.getById(task.id)?.milestoneId
            ?: return
        milestoneDao.touchUpdated(milestoneId, LocalDateTime.now())
    }
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun completionPercent(date: LocalDate): Float = runCatching {
        val total = dao.countForDate(date).takeIf { it > 0 } ?: return@runCatching 0f
        val done = dao.countDoneForDate(date)
        done.toFloat() / total.toFloat()
    }.getOrDefault(0f)
}
