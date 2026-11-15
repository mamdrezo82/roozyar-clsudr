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
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3)],
)
abstract class RoozYarDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao
}
