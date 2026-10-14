package com.meshack.personalmanager.data.database

import androidx.room.TypeConverter
import java.time.LocalDateTime
import com.meshack.personalmanager.domain.DayType
import com.meshack.personalmanager.domain.MilestonePriority
import com.meshack.personalmanager.domain.RecitalSource
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun fromMinuteOfDay(minute: Int): LocalTime =
        LocalTime.of(minute / 60, minute % 60)

    @TypeConverter fun toMinuteOfDay(time: LocalTime): Int =
        time.hour * 60 + time.minute

    @TypeConverter fun fromDayType(value: DayType): String = value.name

    @TypeConverter fun toDayType(value: String): DayType = DayType.valueOf(value)

    @TypeConverter fun fromMilestonePriority(value: MilestonePriority): String = value.name

    @TypeConverter fun toMilestonePriority(value: String): MilestonePriority = MilestonePriority.valueOf(value)

    @TypeConverter fun fromRecitalSource(value: RecitalSource): String = value.name

    @TypeConverter fun toRecitalSource(value: String): RecitalSource = RecitalSource.valueOf(value)

    @TypeConverter fun fromEpochDay(epoch: Long?): LocalDate? =
        epoch?.let(LocalDate::ofEpochDay)

    @TypeConverter fun toEpochDay(date: LocalDate?): Long? =
        date?.toEpochDay()
    @TypeConverter
fun fromLocalDateTime(value: String?): LocalDateTime? =
    value?.let(LocalDateTime::parse)

@TypeConverter
fun toLocalDateTime(value: LocalDateTime?): String? =
    value?.toString()
    }
