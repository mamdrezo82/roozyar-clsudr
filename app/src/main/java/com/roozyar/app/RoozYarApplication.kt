package com.roozyar.app

import android.app.Application
import com.roozyar.app.core.notifications.AchievementTracker
import com.roozyar.app.core.notifications.Notifications
import com.roozyar.app.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class RoozYarApplication : Application() {
    @Inject lateinit var rescheduleAll: RescheduleAllRemindersUseCase
    @Inject lateinit var achievementTracker: AchievementTracker

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        // Keeps reminders correct after app updates, restores and clock/time-zone changes.
        appScope.launch { runCatching { rescheduleAll() } }
        achievementTracker.start(appScope)
    }
}
