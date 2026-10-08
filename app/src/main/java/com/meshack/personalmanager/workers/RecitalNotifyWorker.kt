package com.meshack.personalmanager.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meshack.personalmanager.R
import com.meshack.personalmanager.services.BlockOverlayActivity
import com.meshack.personalmanager.data.repositories.RecitalRepository
import com.meshack.personalmanager.data.local.entities.Recital
import com.meshack.personalmanager.network.RecitalNetworkClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import androidx.hilt.work.HiltWorker

@HiltWorker
class RecitalNotifyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recitalRepository: RecitalRepository,
    private val networkClient: RecitalNetworkClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val recital = pickRecital() ?: return Result.success()
        showOverlay(recital)
        return Result.success()
    }

    private suspend fun pickRecital(): Recital? {
        return runCatching { networkClient.fetchVerseOfDay() }
            .getOrNull()
            ?.randomOrNull()
            ?: pickLocal()
    }

    private suspend fun pickLocal(): Recital? {
        return try {
            val list = recitalRepository.getCustom().first()
            if (list.isEmpty()) null else list.random()
        } catch (_: Throwable) { null }
    }

    private fun showOverlay(recital: Recital) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(CHANNEL, "Recitals", IMPORTANCE_HIGH)
        nm.createNotificationChannel(ch)
        val intent = Intent(applicationContext, BlockOverlayActivity::class.java).apply {
            putExtra("text", recital.text)
        }
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        val pi = PendingIntent.getActivity(applicationContext, 0, intent, flags)
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL)
            .setContentTitle("Empowerment Recital")
            .setContentText(recital.text.take(200))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(Random.nextInt(), notif)
    }

    companion object { const val CHANNEL = "recital_channel" }
}
