package com.roozyar.app.di

import com.roozyar.app.data.repository.GoalRepositoryImpl
import com.roozyar.app.data.repository.HabitRepositoryImpl
import com.roozyar.app.data.repository.JournalRepositoryImpl
import com.roozyar.app.data.repository.TaskRepositoryImpl
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.JournalRepository
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
}
