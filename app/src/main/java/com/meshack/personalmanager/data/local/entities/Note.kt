package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = com.meshack.personalmanager.data.local.entities.RoutineBlock::class,
            parentColumns = ["id"],
            childColumns = ["routine_block_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "routine_block_id") val routineBlockId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
