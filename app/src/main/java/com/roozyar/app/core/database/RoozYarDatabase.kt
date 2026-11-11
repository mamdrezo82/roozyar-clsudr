package com.roozyar.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RoozYarDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao
}
