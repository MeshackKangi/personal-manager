package com.meshack.personalmanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.RoutineBlockRepository
import com.meshack.personalmanager.data.repositories.UsageLogRepository
import com.meshack.personalmanager.services.AppBlockManager
import com.meshack.personalmanager.util.ServiceAliveHolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import androidx.hilt.work.HiltWorker


@HiltWorker
class AccessibilityWatchdogWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appLimitRepository: AppLimitRepository,
    private val usageLogRepository: UsageLogRepository,
    private val routineBlockRepository: RoutineBlockRepository,
    private val appBlockManager: AppBlockManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!ServiceAliveHolder.isAlive) return Result.success()
        repeat(6) {
            try {
                val limits = appLimitRepository.getBlacklisted()
            } catch (_: Throwable) { /* keep watchdog alive */ }
            delay(30_000L)
        }
        return Result.success()
    }
}
