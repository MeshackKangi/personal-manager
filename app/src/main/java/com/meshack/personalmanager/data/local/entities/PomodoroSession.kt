package com.meshack.personalmanager.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: java.time.LocalDate,
    @ColumnInfo(name = "started_at") val startedAt: java.time.LocalDateTime,
    @ColumnInfo(name = "completed_cycles") val completedCycles: Int = 0,
    @ColumnInfo(name = "focus_minutes_total") val focusMinutesTotal: Int = 0
)
