package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.AppLimit
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limits WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): AppLimit?

    @Query("SELECT * FROM app_limits WHERE blacklisted = 1")
    suspend fun getBlacklistedSync(): List<AppLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(limit: AppLimit): Long

    @Query("DELETE FROM app_limits WHERE id = :id")
    suspend fun deleteById(id: Long)
}
