package com.roozyar.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v1 -> v2: tasks may have a date-only deadline, so the time part needs its own flag. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN dueHasTime INTEGER NOT NULL DEFAULT 0")
    }
}
