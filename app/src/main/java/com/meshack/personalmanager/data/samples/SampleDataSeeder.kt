package com.meshack.personalmanager.data.samples

import com.meshack.personalmanager.data.local.entities.AppLimit
import com.meshack.personalmanager.data.local.entities.Goal
import com.meshack.personalmanager.data.local.entities.Milestone
import com.meshack.personalmanager.data.local.entities.Note
import com.meshack.personalmanager.data.local.entities.PomodoroSession
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.data.local.entities.Task
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.GoalRepository
import com.meshack.personalmanager.data.repositories.NoteRepository
import com.meshack.personalmanager.data.repositories.PomodoroRepository
import com.meshack.personalmanager.data.repositories.RoutineBlockRepository
import com.meshack.personalmanager.data.repositories.TaskRepository
import com.meshack.personalmanager.data.settings.SettingsRepository
import com.meshack.personalmanager.domain.DayType
import com.meshack.personalmanager.domain.MilestonePriority
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sample content so every dashboard panel has something real to show.
 * Each category seeds independently when its own tables are empty, so real
 * user data is never blocked by other categories. Guarded by a DataStore
 * flag so a fully-seeded device never re-seeds. All sample rows are plainly
 * named and can be deleted/edited like any other data.
 */
@Singleton
class SampleDataSeeder @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val routineBlockRepository: RoutineBlockRepository,
    private val appLimitRepository: AppLimitRepository,
    private val noteRepository: NoteRepository,
    private val pomodoroRepository: PomodoroRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun seedIfEmpty() {
        if (settingsRepository.sampleSeeded.first()) return
        var seededAll = true
        if (goalRepository.getOpenGoals().first().isEmpty()) {
            seedGoals()
        } else {
            seededAll = false
        }
        if (routineBlockRepository.getBlocksFor(DayType.WEEKDAY).first().isEmpty()) {
            seedRoutine()
        } else {
            seededAll = false
        }
        if (appLimitRepository.getAllLimits().first().isEmpty()) {
            seedBlocking()
        } else {
            seededAll = false
        }
        if (noteRepository.allNotes().first().isEmpty()) {
            seedNotes()
        } else {
            seededAll = false
        }
        if (pomodoroRepository.sessionsSince(LocalDate.now().minusDays(365)).first().isEmpty()) {
            seedFocus()
        } else {
            seededAll = false
        }
        if (seededAll) settingsRepository.setSampleSeeded()
    }

    private suspend fun seedGoals() {
        val now = LocalDateTime.now()
        // Goal 1: ~70% — read 12 books this quarter.
        val g1 = goalRepository.insertGoal(
            Goal(title = "Sample: Read 12 books", updatedAt = now.minusHours(2))
        )
        val m1 = goalRepository.insertMilestone(
            Milestone(
                goalId = g1, title = "Sample: Finish first 3 books",
                notes = "Sample milestone — unfold into tasks.",
                priority = MilestonePriority.HIGH, updatedAt = now.minusHours(2)
            )
        )
        val m2 = goalRepository.insertMilestone(
            Milestone(
                goalId = g1, title = "Sample: Books 4 to 6",
                notes = "Sample milestone.", priority = MilestonePriority.MEDIUM,
                updatedAt = now.minusDays(1)
            )
        )
        val today = LocalDate.now()
        // 5 of 7 done -> ~71%.
        val doneTitles = listOf(
            "Sample: Pick book one", "Sample: Read chapters 1-3",
            "Sample: Read chapters 4-6", "Sample: Write margin notes",
            "Sample: Pick book two"
        )
        doneTitles.forEach { title ->
            taskRepository.insert(Task(title = title, milestoneId = m1, forDate = today, done = true))
        }
        val t1 = taskRepository.insert(
            Task(title = "Sample: Read chapters 7-9", milestoneId = m1, forDate = today, done = false)
        )
        // Nested subtask example.
        taskRepository.insert(
            Task(title = "Sample: Look up two references", milestoneId = m1, parentTaskId = t1, forDate = today, done = false)
        )
        taskRepository.insert(
            Task(title = "Sample: Pick book four", milestoneId = m2, forDate = today, done = false)
        )
        // Goal 2: ~20% — run a 10k.
        val g2 = goalRepository.insertGoal(
            Goal(title = "Sample: Run a 10k", updatedAt = now.minusDays(2))
        )
        val m3 = goalRepository.insertMilestone(
            Milestone(
                goalId = g2, title = "Sample: Couch to 5k",
                notes = "Sample milestone.", priority = MilestonePriority.MEDIUM,
                updatedAt = now.minusDays(2)
            )
        )
        taskRepository.insert(Task(title = "Sample: Week 1 runs", milestoneId = m3, forDate = today, done = true))
        repeat(4) { i ->
            taskRepository.insert(
                Task(title = "Sample: Week ${i + 2} runs", milestoneId = m3, forDate = today, done = false)
            )
        }
        goalRepository.touchGoal(g1)
    }

    private suspend fun seedRoutine() {
        val blocks = listOf(
            RoutineBlock(name = "Sample: Morning pages", dayType = DayType.WEEKDAY, startMinute = 6 * 60, endMinute = 7 * 60, isFreeTime = false),
            RoutineBlock(name = "Sample: Deep work", dayType = DayType.WEEKDAY, startMinute = 9 * 60, endMinute = 12 * 60, isFreeTime = false),
            RoutineBlock(name = "Sample: Evening walk", dayType = DayType.WEEKDAY, startMinute = 18 * 60, endMinute = 19 * 60, isFreeTime = true)
        )
        blocks.forEach { runCatching { routineBlockRepository.insertWithValidation(it) } }
    }

    private suspend fun seedBlocking() {
        appLimitRepository.upsert(
            AppLimit(packageName = "com.instagram.android", appName = "Sample: Instagram", dailyLimitMinutes = 30)
        )
        appLimitRepository.upsert(
            AppLimit(packageName = "com.zhiliaoapp.musically", appName = "Sample: TikTok", dailyLimitMinutes = 20)
        )
    }

    private suspend fun seedNotes() {
        noteRepository.insert(
            Note(title = "Sample: Welcome note", content = "Sample note — edit or delete me from Notes.")
        )
        noteRepository.insert(
            Note(title = "Sample: Idea backlog", content = "Sample note — park ideas here.")
        )
    }

    private suspend fun seedFocus() {
        val today = LocalDate.now()
        pomodoroRepository.insert(
            PomodoroSession(date = today, startedAt = LocalDateTime.now().minusHours(3), completedCycles = 3, focusMinutesTotal = 75)
        )
        pomodoroRepository.insert(
            PomodoroSession(date = today.minusDays(1), startedAt = LocalDateTime.now().minusDays(1), completedCycles = 2, focusMinutesTotal = 50)
        )
        pomodoroRepository.insert(
            PomodoroSession(date = today.minusDays(2), startedAt = LocalDateTime.now().minusDays(2), completedCycles = 4, focusMinutesTotal = 100)
        )
    }
}
