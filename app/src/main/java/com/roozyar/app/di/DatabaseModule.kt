package com.roozyar.app.di

import android.content.Context
import androidx.room.Room
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
        Room.databaseBuilder(context, RoozYarDatabase::class.java, "roozyar.db").build()

    @Provides fun provideTaskDao(db: RoozYarDatabase): TaskDao = db.taskDao()
    @Provides fun provideUserProfileDao(db: RoozYarDatabase): UserProfileDao = db.userProfileDao()
}
