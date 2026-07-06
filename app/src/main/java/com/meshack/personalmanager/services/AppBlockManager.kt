package com.meshack.personalmanager.services

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.RoutineBlockRepository
import com.meshack.personalmanager.data.repositories.UsageLogRepository
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlockManager @Inject constructor(
    private val context: Context,
    private val appLimitRepository: AppLimitRepository,
    private val routineBlockRepository: RoutineBlockRepository,
    private val usageLogRepository: UsageLogRepository,
    private val coroutineScope: CoroutineScope
) {
    private var pollingJob: Job? = null
    private val blockedPackages = mutableSetOf<String>()

    fun startMonitoring() {
        if (pollingJob?.isActive == true) return
        pollingJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                evaluateForegroundApp()
                delay(2000)
            }
        }
    }

    fun stopMonitoring() {
        pollingJob?.cancel(); pollingJob = null
    }

    suspend fun evaluateForegroundApp() = withContext(Dispatchers.Default) {
        val foreground = getForegroundAppPackage() ?: return@withContext
        val activeBlock = routineBlockRepository.getCurrentActiveBlock(java.time.LocalDate.now())
        val limits = appLimitRepository.getBlacklistedSync()
        val blacklist = limits.map { it.packageName }

        if (foreground in blacklist) {
            val today = java.time.LocalDate.now()
            val limit = limits.firstOrNull { it.packageName == foreground } ?: return@withContext
            if (activeBlock == null || !activeBlock.isFreeTime) {
                launchBlock(foreground)
                return@withContext
            }
            val used = usageLogRepository.getUsageMinutes(foreground, today)
            if (used >= limit.dailyLimitMinutes) {
                // Free time but limit reached — find next free-time block to unlock
                val nextFree = routineBlockRepository.getNextFreeTimeBlock(activeBlock.endMinute, activeBlock.dayType)
                if (nextFree == null) launchBlock(foreground)
                return@withContext
            }
        }
    }

    private fun getForegroundAppPackage(): String? {
        return try {
            val mgr = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = mgr.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, now - 60_000, now
            )
            stats.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (t: Throwable) {
            null
        }
    }

    private suspend fun launchBlock(packageName: String) {
        if (packageName in blockedPackages) return
        blockedPackages += packageName
        withContext(Dispatchers.Main) {
            val intent = Intent(context, BlockOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("package", packageName)
            }
            context.startActivity(intent)
        }
    }

    fun onBlockDismissed(packageName: String) {
        blockedPackages -= packageName
    }
}
