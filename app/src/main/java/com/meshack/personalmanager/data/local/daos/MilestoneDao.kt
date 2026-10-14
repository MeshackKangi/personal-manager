package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meshack.personalmanager.data.local.entities.Milestone
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE goal_id = :goalId ORDER BY updated_at DESC")
    fun getByGoal(goalId: Long): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones ORDER BY updated_at DESC")
    fun getAll(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Milestone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milestone: Milestone): Long

    @Update
    suspend fun update(milestone: Milestone)

    @Query("UPDATE milestones SET updated_at = :updatedAt WHERE id = :id")
    suspend fun touchUpdated(id: Long, updatedAt: java.time.LocalDateTime)
}
