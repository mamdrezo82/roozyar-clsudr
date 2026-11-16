package com.roozyar.app.di

import com.roozyar.app.core.notifications.WorkReminderScheduler
import com.roozyar.app.domain.reminders.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: WorkReminderScheduler): ReminderScheduler
}
