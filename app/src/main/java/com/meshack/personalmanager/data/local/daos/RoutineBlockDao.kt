package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.domain.DayType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RoutineBlockDao {
    @Query("SELECT * FROM routine_blocks WHERE dayType = :dayType ORDER BY start_minute")
    fun getBlocksForDayType(dayType: DayType): Flow<List<RoutineBlock>>

    @Query("SELECT * FROM routine_blocks WHERE dayType = :dayType AND start_minute <= :nowMinute AND end_minute > :nowMinute LIMIT 1")
    suspend fun getActiveBlockForDayType(dayType: DayType, nowMinute: Int): RoutineBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: RoutineBlock): Long

    @Delete
    suspend fun delete(block: RoutineBlock)

    @Query("SELECT * FROM routine_blocks WHERE dayType = :dayType AND start_minute >= :minute ORDER BY start_minute LIMIT 1")
    suspend fun getNextFreeTimeBlockAfter(dayType: DayType, minute: Int): RoutineBlock?

    @Query("SELECT * FROM routine_blocks WHERE dayType = :dayType ORDER BY start_minute")
    suspend fun getAllBlocksForDayTypeSync(dayType: DayType): List<RoutineBlock>

    @Query("SELECT COUNT(*) FROM routine_blocks WHERE dayType = :dayType AND start_minute < :end AND end_minute > :start")
    suspend fun countOverlapping(dayType: DayType, start: Int, end: Int): Int
}
