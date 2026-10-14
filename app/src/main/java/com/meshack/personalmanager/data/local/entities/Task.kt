package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = RoutineBlock::class,
            parentColumns = ["id"],
            childColumns = ["routine_block_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Milestone::class,
            parentColumns = ["id"],
            childColumns = ["milestone_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("routine_block_id"),
        Index("milestone_id"),
        Index("parent_task_id")
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "routine_block_id") val routineBlockId: Long? = null,
    @ColumnInfo(name = "milestone_id") val milestoneId: Long? = null,
    @ColumnInfo(name = "parent_task_id") val parentTaskId: Long? = null,
    @ColumnInfo(name = "for_date") val forDate: LocalDate,
    val done: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
