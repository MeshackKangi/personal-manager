package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.UsageLog
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs WHERE package_name = :packageName AND date = :date LIMIT 1")
    suspend fun getForAppOnDate(packageName: String, date: java.time.LocalDate): UsageLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: UsageLog): Long

    @Query("UPDATE usage_logs SET minutes_used = :minutes WHERE package_name = :packageName AND date = :date")
    suspend fun updateMinutes(packageName: String, date: java.time.LocalDate, minutes: Int)

    @Query("DELETE FROM usage_logs WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: java.time.LocalDate)

    @Query("SELECT * FROM usage_logs WHERE package_name = :packageName ORDER BY date DESC")
    fun getHistoryForApp(packageName: String): Flow<List<UsageLog>>
}
