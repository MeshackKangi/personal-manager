package com.meshack.personalmanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.meshack.personalmanager.domain.MilestonePriority
import java.time.LocalDate

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goal_id")]
)
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    val title: String,
    @ColumnInfo(name = "target_date") val targetDate: LocalDate? = null,
    val notes: String = "",
    val priority: MilestonePriority = MilestonePriority.MEDIUM,
    @ColumnInfo(name = "updated_at") val updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    @ColumnInfo(name = "created_at") val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
