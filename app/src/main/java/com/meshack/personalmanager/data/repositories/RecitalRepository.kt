package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.RecitalDao
import com.meshack.personalmanager.data.local.entities.Recital
import com.meshack.personalmanager.domain.RecitalSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecitalRepository @Inject constructor(private val dao: RecitalDao) {
    fun getAllRecitals(): Flow<List<Recital>> = dao.getAllRecitals()
    fun getCustom(): Flow<List<Recital>> = dao.getCustomRecitals()
    suspend fun insert(recital: Recital) = dao.insert(recital)
}
