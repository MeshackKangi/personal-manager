package com.meshack.personalmanager.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meshack.personalmanager.data.local.entities.Recital
import kotlinx.coroutines.flow.Flow

@Dao
interface RecitalDao {
    @Query("SELECT * FROM recitals ORDER BY created_at DESC")
    fun getAllRecitals(): Flow<List<Recital>>

    @Query("SELECT * FROM recitals WHERE source = 'CUSTOM' ORDER BY created_at DESC")
    fun getCustomRecitals(): Flow<List<Recital>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recital: Recital): Long
}
