package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    val date: java.time.LocalDate,
    @ColumnInfo(name = "minutes_used") val minutesUsed: Int
)
