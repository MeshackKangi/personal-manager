package com.meshack.personalmanager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meshack.personalmanager.data.local.daos.AppLimitDao
import com.meshack.personalmanager.data.local.daos.GoalDao
import com.meshack.personalmanager.data.local.daos.MilestoneDao
import com.meshack.personalmanager.data.local.daos.NoteDao
import com.meshack.personalmanager.data.local.daos.PomodoroSessionDao
import com.meshack.personalmanager.data.local.daos.RecitalDao
import com.meshack.personalmanager.data.local.daos.RoutineBlockDao
import com.meshack.personalmanager.data.local.daos.TaskDao
import com.meshack.personalmanager.data.local.daos.UsageLogDao
import com.meshack.personalmanager.data.local.entities.AppLimit
import com.meshack.personalmanager.data.local.entities.Goal
import com.meshack.personalmanager.data.local.entities.Milestone
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
        PomodoroSession::class,
        Goal::class,
        Milestone::class
    ],
    version = 2,
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
    abstract fun goalDao(): GoalDao
    abstract fun milestoneDao(): MilestoneDao

    companion object {
        const val NAME = "personal_manager.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS goals (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "target_date INTEGER, " +
                        "archived INTEGER NOT NULL DEFAULT 0, " +
                        "created_at TEXT NOT NULL, " +
                        "updated_at TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS milestones (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "goal_id INTEGER NOT NULL REFERENCES goals(id) ON DELETE CASCADE, " +
                        "title TEXT NOT NULL, " +
                        "target_date INTEGER, " +
                        "notes TEXT NOT NULL DEFAULT '', " +
                        "priority TEXT NOT NULL DEFAULT 'MEDIUM', " +
                        "updated_at TEXT NOT NULL, " +
                        "created_at TEXT NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_milestones_goal_id ON milestones(goal_id)")
                // SQLite cannot ADD a foreign key with ALTER TABLE, so rebuild tasks
                // with the milestone FK plus the new columns and indices.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tasks_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "routine_block_id INTEGER, " +
                        "milestone_id INTEGER, " +
                        "parent_task_id INTEGER, " +
                        "for_date INTEGER NOT NULL, " +
                        "done INTEGER NOT NULL, " +
                        "created_at TEXT NOT NULL, " +
                        "FOREIGN KEY(routine_block_id) REFERENCES routine_blocks(id) ON UPDATE NO ACTION ON DELETE SET NULL, " +
                        "FOREIGN KEY(milestone_id) REFERENCES milestones(id) ON UPDATE NO ACTION ON DELETE SET NULL)"
                )
                db.execSQL(
                    "INSERT INTO tasks_new " +
                        "(id, title, routine_block_id, milestone_id, parent_task_id, for_date, done, created_at) " +
                        "SELECT id, title, routine_block_id, NULL, NULL, for_date, done, created_at FROM tasks"
                )
                db.execSQL("DROP TABLE tasks")
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_routine_block_id ON tasks(routine_block_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_milestone_id ON tasks(milestone_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_parent_task_id ON tasks(parent_task_id)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(application: android.app.Application): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(application, AppDatabase::class.java, NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
