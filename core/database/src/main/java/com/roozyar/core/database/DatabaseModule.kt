package com.roozyar.core.database

import android.content.Context
import androidx.room.Room
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

    @Provides fun provideTaskDao(database: RoozYarDatabase): TaskDao = database.taskDao()
    @Provides fun provideUserProfileDao(database: RoozYarDatabase): UserProfileDao = database.userProfileDao()
}
