package com.meshack.personalmanager.di

import android.app.Application
import androidx.room.Room
import com.meshack.personalmanager.data.database.AppDatabase
import com.meshack.personalmanager.data.local.daos.AppLimitDao
import com.meshack.personalmanager.data.local.daos.GoalDao
import com.meshack.personalmanager.data.local.daos.MilestoneDao
import com.meshack.personalmanager.data.local.daos.NoteDao
import com.meshack.personalmanager.data.local.daos.PomodoroSessionDao
import com.meshack.personalmanager.data.local.daos.RecitalDao
import com.meshack.personalmanager.data.local.daos.RoutineBlockDao
import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.daos.UsageLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideRoutineBlockDao(db: AppDatabase): RoutineBlockDao = db.routineBlockDao()
    @Provides fun provideAppLimitDao(db: AppDatabase): AppLimitDao = db.appLimitDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideRecitalDao(db: AppDatabase): RecitalDao = db.recitalDao()
    @Provides fun provideUsageLogDao(db: AppDatabase): UsageLogDao = db.usageLogDao()
    @Provides fun providePomodoroSessionDao(db: AppDatabase): PomodoroSessionDao = db.pomodoroSessionDao()
    @Provides fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()
    @Provides fun provideMilestoneDao(db: AppDatabase): MilestoneDao = db.milestoneDao()
}
