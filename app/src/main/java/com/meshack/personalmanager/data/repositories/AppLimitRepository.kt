package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.AppLimitDao
import com.meshack.personalmanager.data.local.entities.AppLimit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppLimitRepository @Inject constructor(private val dao: AppLimitDao) {
    fun getAllLimits(): Flow<List<AppLimit>> = dao.getAllLimits()
    suspend fun getBlacklisted(): List<AppLimit> = dao.getBlacklistedSync()
    suspend fun getByPackage(packageName: String): AppLimit? = dao.getByPackage(packageName)
    suspend fun upsert(limit: AppLimit) = dao.upsert(limit)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
