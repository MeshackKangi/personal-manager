package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.entities.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class TaskRepository @Inject constructor(private val dao: TaskDao) {
    fun getTasksFor(date: LocalDate): Flow<List<Task>> = dao.getTasksForDate(date)
    fun getTasksForBlock(date: LocalDate, blockId: Long?): Flow<List<Task>> = dao.getTasksForBlock(date, blockId)
    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun markDone(id: Long, done: Boolean) = dao.markDone(id, done)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun completionPercent(date: LocalDate): Float = runCatching {
        val total = dao.countForDate(date).takeIf { it > 0 } ?: return@runCatching 0f
        val done = dao.countDoneForDate(date)
        done.toFloat() / total.toFloat()
    }.getOrDefault(0f)
}
