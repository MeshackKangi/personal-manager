package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    val appName: String,
    @ColumnInfo(name = "daily_limit_minutes") val dailyLimitMinutes: Int,
    val blacklisted: Boolean = true
)
