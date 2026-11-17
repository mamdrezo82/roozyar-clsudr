package com.roozyar.app.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        UserProfileEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        JournalEntryEntity::class,
        EventEntity::class,
        FocusSessionEntity::class,
        UnlockedAchievementEntity::class,
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3), AutoMigration(from = 3, to = 4), AutoMigration(from = 4, to = 5), AutoMigration(from = 5, to = 6), AutoMigration(from = 6, to = 7)],
)
abstract class RoozYarDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun journalDao(): JournalDao
    abstract fun eventDao(): EventDao
    abstract fun backupDao(): BackupDao
    abstract fun focusDao(): FocusDao
}
