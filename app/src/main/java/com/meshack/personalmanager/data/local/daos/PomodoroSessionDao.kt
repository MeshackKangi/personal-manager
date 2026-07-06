package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): PomodoroSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSession): Long

    @Query("SELECT * FROM pomodoro_sessions WHERE date >= :since ORDER BY date DESC")
    fun getSince(since: LocalDate): Flow<List<PomodoroSession>>
}
