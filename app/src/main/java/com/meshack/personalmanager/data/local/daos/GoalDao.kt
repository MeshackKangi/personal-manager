package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meshack.personalmanager.data.local.entities.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE archived = 0 ORDER BY updated_at DESC")
    fun getOpenGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY updated_at DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Query("UPDATE goals SET updated_at = :updatedAt WHERE id = :id")
    suspend fun touchUpdated(id: Long, updatedAt: java.time.LocalDateTime)
}
