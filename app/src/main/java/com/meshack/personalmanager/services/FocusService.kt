package com.meshack.personalmanager.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import com.meshack.personalmanager.data.repositories.PomodoroRepository
import com.meshack.personalmanager.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Owns the focus countdown so it survives the app going to background.
 * The 5-minute break (and 15-minute long break) fire from here, and a live
 * notification banner always shows the phase plus remaining time.
 */
@AndroidEntryPoint
class FocusService : Service() {

    @Inject lateinit var pomodoroRepository: PomodoroRepository
    @Inject lateinit var focusState: FocusStateHolder
    @Inject lateinit var applicationScope: CoroutineScope

    private var ticker: Job? = null
    private var todaySession: PomodoroSession? = null
    private var workSecondsTotal: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        when (intent?.action) {
            ACTION_START -> startTicking()
            ACTION_PAUSE -> pauseTicking()
            ACTION_RESET -> resetTimer()
        }
        android.util.Log.d(TAG, "command=${intent?.action}")
        return START_NOT_STICKY
    }

    private fun startTicking() {
        if (todaySession == null) {
            todaySession = PomodoroSession(date = LocalDate.now(), startedAt = LocalDateTime.now())
            applicationScope.launch { saveSession() }
        }
        focusState.update { it.copy(isRunning = true) }
        if (ticker?.isActive == true) {
            publishNotification()
            return
        }
        startForeground(NOTIF_ID, buildNotification(focusState.state.value))
        ticker = applicationScope.launch {
            while (isActive) {
                val current = focusState.state.value
                if (!current.isRunning) break
                delay(1000L)
                val snapshot = focusState.state.value
                if (!snapshot.isRunning) break
                if (snapshot.remainingSeconds > 0) {
                    if (snapshot.phase == FocusPhase.WORK) workSecondsTotal++
                    focusState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                } else {
                    advancePhase(snapshot)
                }
                publishNotification()
            }
        }
    }

    private suspend fun advancePhase(snapshot: FocusState) {
        when (snapshot.phase) {
            FocusPhase.WORK -> {
                val cycles = snapshot.completedCycles + 1
                focusState.update { it.copy(completedCycles = cycles) }
                saveSession()
                android.util.Log.d(TAG, "work complete, cycles=$cycles")
                if (cycles >= 4) {
                    focusState.update {
                        it.copy(phase = FocusPhase.LONG_BREAK, remainingSeconds = FocusState.LONG_BREAK_SECONDS)
                    }
                } else {
                    focusState.update {
                        it.copy(phase = FocusPhase.BREAK, remainingSeconds = FocusState.BREAK_SECONDS)
                    }
                }
            }
            FocusPhase.BREAK -> {
                focusState.update {
                    it.copy(phase = FocusPhase.WORK, remainingSeconds = FocusState.WORK_SECONDS)
                }
            }
            FocusPhase.LONG_BREAK -> {
                focusState.update {
                    it.copy(
                        phase = FocusPhase.WORK,
                        remainingSeconds = FocusState.WORK_SECONDS,
                        completedCycles = 0
                    )
                }
            }
        }
    }

    private fun pauseTicking() {
        focusState.update { it.copy(isRunning = false) }
        ticker?.cancel()
        ticker = null
        applicationScope.launch { saveSession() }
        publishNotification()
    }

    private fun resetTimer() {
        ticker?.cancel()
        ticker = null
        val phase = focusState.state.value.phase
        val seconds = when (phase) {
            FocusPhase.WORK -> FocusState.WORK_SECONDS
            FocusPhase.BREAK -> FocusState.BREAK_SECONDS
            FocusPhase.LONG_BREAK -> FocusState.LONG_BREAK_SECONDS
        }
        focusState.update { it.copy(isRunning = false, remainingSeconds = seconds) }
        applicationScope.launch { saveSession() }
        stopSelf()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private suspend fun saveSession() {
        todaySession?.let { session ->
            val cycles = focusState.state.value.completedCycles
            pomodoroRepository.insert(
                session.copy(completedCycles = cycles, focusMinutesTotal = workSecondsTotal / 60)
            )
        }
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Focus timer", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private fun publishNotification() {
        try {
            val notif = buildNotification(focusState.state.value)
            val manager = getSystemService(NotificationManager::class.java)
            // Promote to foreground on first publish; updates afterwards.
            try {
                startForeground(NOTIF_ID, notif)
            } catch (_: Throwable) {
                manager.notify(NOTIF_ID, notif)
            }
        } catch (_: Throwable) { /* banner is best-effort */
        }
    }

    private fun buildNotification(state: FocusState): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mm = state.remainingSeconds / 60
        val ss = state.remainingSeconds % 60
        val status = if (state.isRunning) "left" else "paused"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Focus · ${state.phase.name}")
            .setContentText("${mm}m ${ss}s $status — tap to return")
            .setContentIntent(pending)
            .setOngoing(state.isRunning)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    override fun onDestroy() {
        ticker?.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "focus_channel"
        const val NOTIF_ID = 2001
        private const val TAG = "FocusService"

        fun startIntent(context: Context): Intent =
            Intent(context, FocusService::class.java).setAction(ACTION_START)

        fun pauseIntent(context: Context): Intent =
            Intent(context, FocusService::class.java).setAction(ACTION_PAUSE)

        fun resetIntent(context: Context): Intent =
            Intent(context, FocusService::class.java).setAction(ACTION_RESET)

        private const val ACTION_START = "com.meshack.personalmanager.focus.START"
        private const val ACTION_PAUSE = "com.meshack.personalmanager.focus.PAUSE"
        private const val ACTION_RESET = "com.meshack.personalmanager.focus.RESET"
    }
}
