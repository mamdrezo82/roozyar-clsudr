package com.roozyar.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** Bulk read / wipe / insert used only by backup and restore. Insert keeps the ids it is given. */
@Dao
interface BackupDao {
    @Query("SELECT * FROM tasks") suspend fun tasks(): List<TaskEntity>
    @Query("SELECT * FROM habits") suspend fun habits(): List<HabitEntity>
    @Query("SELECT * FROM habit_logs") suspend fun habitLogs(): List<HabitLogEntity>
    @Query("SELECT * FROM goals") suspend fun goals(): List<GoalEntity>
    @Query("SELECT * FROM milestones") suspend fun milestones(): List<MilestoneEntity>
    @Query("SELECT * FROM journal_entries") suspend fun journal(): List<JournalEntryEntity>
    @Query("SELECT * FROM events") suspend fun events(): List<EventEntity>
    @Query("SELECT * FROM user_profile") suspend fun profiles(): List<UserProfileEntity>
    @Query("SELECT * FROM focus_sessions") suspend fun focusSessions(): List<FocusSessionEntity>
    @Query("SELECT * FROM achievements_unlocked") suspend fun unlockedAchievements(): List<UnlockedAchievementEntity>

    @Query("DELETE FROM habit_logs") suspend fun clearHabitLogs()
    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM milestones") suspend fun clearMilestones()
    @Query("DELETE FROM goals") suspend fun clearGoals()
    @Query("DELETE FROM tasks") suspend fun clearTasks()
    @Query("DELETE FROM journal_entries") suspend fun clearJournal()
    @Query("DELETE FROM events") suspend fun clearEvents()
    @Query("DELETE FROM user_profile") suspend fun clearProfiles()
    @Query("DELETE FROM focus_sessions") suspend fun clearFocusSessions()
    @Query("DELETE FROM achievements_unlocked") suspend fun clearUnlockedAchievements()

    @Insert suspend fun insertTasks(items: List<TaskEntity>)
    @Insert suspend fun insertHabits(items: List<HabitEntity>)
    @Insert suspend fun insertHabitLogs(items: List<HabitLogEntity>)
    @Insert suspend fun insertGoals(items: List<GoalEntity>)
    @Insert suspend fun insertMilestones(items: List<MilestoneEntity>)
    @Insert suspend fun insertJournal(items: List<JournalEntryEntity>)
    @Insert suspend fun insertEvents(items: List<EventEntity>)
    @Insert suspend fun insertProfiles(items: List<UserProfileEntity>)
    @Insert suspend fun insertFocusSessions(items: List<FocusSessionEntity>)
    @Insert suspend fun insertUnlockedAchievements(items: List<UnlockedAchievementEntity>)
}
