package com.roozyar.app.di

import com.roozyar.app.data.backup.BackupRepositoryImpl
import com.roozyar.app.data.repository.EventRepositoryImpl
import com.roozyar.app.data.repository.FocusRepositoryImpl
import com.roozyar.app.data.repository.GoalRepositoryImpl
import com.roozyar.app.data.repository.HabitRepositoryImpl
import com.roozyar.app.data.repository.JournalRepositoryImpl
import com.roozyar.app.data.repository.ProfileRepositoryImpl
import com.roozyar.app.data.repository.TaskRepositoryImpl
import com.roozyar.app.domain.repository.BackupRepository
import com.roozyar.app.domain.repository.EventRepository
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.JournalRepository
import com.roozyar.app.domain.repository.ProfileRepository
import com.roozyar.app.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    @Singleton
    abstract fun bindFocusRepository(impl: FocusRepositoryImpl): FocusRepository
}
