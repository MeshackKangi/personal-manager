package com.meshack.personalmanager.workers

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.UsageLogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AssistedInject
class UsageAggregatorWorker @Inject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appLimitRepository: AppLimitRepository,
    private val usageLogRepository: UsageLogRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val limits = appLimitRepository.getBlacklistedSync().first()
            val mgr = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = System.currentTimeMillis()
            val stats = mgr.queryAndAggregateUsageStats(start, end) ?: return Result.success()

            for (limit in limits) {
                val total = stats[limit.packageName] ?: continue
                val minutes = (total / 1000.0 / 60.0).toInt()
                usageLogRepository.addMinutes(limit.packageName, today, minutes)
            }
        } catch (_: Throwable) { /* nonfatal */ }
        return Result.success()
    }
}
