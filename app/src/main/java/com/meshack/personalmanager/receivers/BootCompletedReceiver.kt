package com.meshack.personalmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meshack.personalmanager.workers.AccessibilityWatchdogWorker
import com.meshack.personalmanager.workers.RecitalNotifyWorker
import com.meshack.personalmanager.workers.UsageAggregatorWorker
import java.util.concurrent.TimeUnit

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val wm = WorkManager.getInstance(context)

        val watchdog = PeriodicWorkRequestBuilder<AccessibilityWatchdogWorker>(15, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork("watchdog", ExistingPeriodicWorkPolicy.KEEP, watchdog)

        val aggregator = PeriodicWorkRequestBuilder<UsageAggregatorWorker>(15, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork("usage_aggregator", ExistingPeriodicWorkPolicy.KEEP, aggregator)

        val recital = PeriodicWorkRequestBuilder<RecitalNotifyWorker>(3, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork("recital_notify", ExistingPeriodicWorkPolicy.KEEP, recital)
    }
}
