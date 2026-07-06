package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.RoutineBlockDao
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.domain.DayType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class RoutineBlockRepository @Inject constructor(
    private val dao: RoutineBlockDao
) {
    fun getBlocksFor(dayType: DayType): Flow<List<RoutineBlock>> =
        dao.getBlocksForDayType(dayType)

    suspend fun getCurrentActiveBlock(now: LocalDate): RoutineBlock? {
        val dayType = if (now.dayOfWeek.value >= 6) DayType.WEEKEND else DayType.WEEKDAY
        val minuteOfDay = LocalTime.now().hour * 60 + LocalTime.now().minute
        return dao.getActiveBlockForDayType(dayType, minuteOfDay)
    }

    /** Returns the next block with isFreeTime=true after the given minute, or null. */
    suspend fun getNextFreeTimeBlock(fromMinute: Int, dayType: DayType): RoutineBlock? =
        dao.getNextFreeTimeBlockAfter(dayType, fromMinute)

    /** Validates no overlap with other blocks of the same dayType before saving. */
    suspend fun insertWithValidation(block: RoutineBlock): Result<Long> = runCatching {
        val blocks = dao.getAllBlocksForDayTypeSync(block.dayType)
        val endExclusive = if (block.endMinute > 0 && block.endMinute <= 1440) block.endMinute else 1440
        val overlapping = blocks.count { it.id != block.id ->
            it.startMinute < endExclusive && it.endMinute > block.startMinute
        }
        if (overlapping > 0) {
            throw IllegalArgumentException("Block overlaps with an existing block for ${block.dayType.name}")
        }
        dao.insert(block)
    }

    suspend fun delete(block: RoutineBlock) = dao.delete(block)
}
