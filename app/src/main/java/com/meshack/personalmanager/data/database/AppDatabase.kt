package com.meshack.personalmanager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meshack.personalmanager.data.local.daos.AppLimitDao
import com.meshack.personalmanager.data.local.daos.NoteDao
import com.meshack.personalmanager.data.local.daos.PomodoroSessionDao
import com.meshack.personalmanager.data.local.daos.RecitalDao
import com.meshack.personalmanager.data.local.daos.RoutineBlockDao
import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.daos.UsageLogDao
import com.meshack.personalmanager.data.local.entities.AppLimit
import com.meshack.personalmanager.data.local.entities.Note
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import com.meshack.personalmanager.data.local.entities.Recital
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.data.local.entities.Task
import com.meshack.personalmanager.data.local.entities.UsageLog

@Database(
    entities = [
        RoutineBlock::class,
        AppLimit::class,
        Task::class,
        Note::class,
        Recital::class,
        UsageLog::class,
        PomodoroSession::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineBlockDao(): RoutineBlockDao
    abstract fun appLimitDao(): AppLimitDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun recitalDao(): RecitalDao
    abstract fun usageLogDao(): UsageLogDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        const val NAME = "personal_manager.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(application: android.app.Application): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(application, AppDatabase::class.java, NAME).build().also { INSTANCE = it }
            }
    }
}
