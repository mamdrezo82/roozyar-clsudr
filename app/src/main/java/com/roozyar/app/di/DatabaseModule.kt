package com.roozyar.app.di

import android.content.Context
import androidx.room.Room
import com.roozyar.app.core.database.BackupDao
import com.roozyar.app.core.database.EventDao
import com.roozyar.app.core.database.FocusDao
import com.roozyar.app.core.database.GoalDao
import com.roozyar.app.core.database.HabitDao
import com.roozyar.app.core.database.JournalDao
import com.roozyar.app.core.database.MIGRATION_1_2
import com.roozyar.app.core.database.RoozYarDatabase
import com.roozyar.app.core.database.TaskDao
import com.roozyar.app.core.database.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoozYarDatabase =
        Room.databaseBuilder(context, RoozYarDatabase::class.java, "roozyar.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun provideTaskDao(db: RoozYarDatabase): TaskDao = db.taskDao()
    @Provides fun provideFocusDao(db: RoozYarDatabase): FocusDao = db.focusDao()
    @Provides fun provideBackupDao(db: RoozYarDatabase): BackupDao = db.backupDao()
    @Provides fun provideEventDao(db: RoozYarDatabase): EventDao = db.eventDao()
    @Provides fun provideJournalDao(db: RoozYarDatabase): JournalDao = db.journalDao()
    @Provides fun provideGoalDao(db: RoozYarDatabase): GoalDao = db.goalDao()
    @Provides fun provideHabitDao(db: RoozYarDatabase): HabitDao = db.habitDao()
    @Provides fun provideUserProfileDao(db: RoozYarDatabase): UserProfileDao = db.userProfileDao()
}
