package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.PomodoroSessionDao
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class PomodoroRepository @Inject constructor(private val dao: PomodoroSessionDao) {
    suspend fun getForDate(date: LocalDate) = dao.getForDate(date)
    fun sessionsSince(since: LocalDate): Flow<List<PomodoroSession>> = dao.getSince(since)
    suspend fun insert(session: PomodoroSession) = dao.insert(session)
}
