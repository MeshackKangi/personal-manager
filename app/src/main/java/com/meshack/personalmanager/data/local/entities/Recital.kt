package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meshack.personalmanager.domain.RecitalSource

@Entity(tableName = "recitals")
data class Recital(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val source: RecitalSource,
    @ColumnInfo(name = "last_shown_at") val lastShownAt: java.time.LocalDateTime? = null,
    @ColumnInfo(name = "created_at") val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
