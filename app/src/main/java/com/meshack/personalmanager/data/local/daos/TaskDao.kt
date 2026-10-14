package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE for_date = :date ORDER BY created_at")
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE milestone_id = :milestoneId ORDER BY created_at")
    fun getByMilestone(milestoneId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE parent_task_id = :parentId ORDER BY created_at")
    suspend fun getSubtasks(parentId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE for_date = :date AND routine_block_id = :blockId")
    fun getTasksForBlock(date: LocalDate, blockId: Long?): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Query("UPDATE tasks SET done = :done WHERE id = :id")
    suspend fun markDone(id: Long, done: Boolean)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE for_date = :date")
    suspend fun countForDate(date: LocalDate): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE for_date = :date AND done = 1")
    suspend fun countDoneForDate(date: LocalDate): Int
}
