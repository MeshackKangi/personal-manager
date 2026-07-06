package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.UsageLogDao
import com.meshack.personalmanager.data.local.entities.UsageLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class UsageLogRepository @Inject constructor(private val dao: UsageLogDao) {
    suspend fun getUsageMinutes(packageName: String, date: LocalDate): Int =
        dao.getForAppOnDate(packageName, date)?.minutesUsed ?: 0

    suspend fun addMinutes(packageName: String, date: LocalDate, minutes: Int) {
        val existing = dao.getForAppOnDate(packageName, date)
        if (existing == null) {
            dao.insert(UsageLog(packageName = packageName, date = date, minutesUsed = minutes))
        } else {
            dao.updateMinutes(packageName, date, existing.minutesUsed + minutes)
        }
    }

    fun getHistory(packageName: String): Flow<List<UsageLog>> = dao.getHistoryForApp(packageName)
}
