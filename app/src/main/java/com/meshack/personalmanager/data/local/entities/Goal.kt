package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "target_date") val targetDate: LocalDate? = null,
    val archived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
