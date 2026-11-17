package com.roozyar.app.data.backup

import androidx.room.withTransaction
import com.roozyar.app.core.database.BackupDao
import com.roozyar.app.core.database.RoozYarDatabase
import com.roozyar.app.domain.repository.BackupCounts
import com.roozyar.app.domain.repository.BackupRepository
import java.time.Clock
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val db: RoozYarDatabase,
    private val dao: BackupDao,
    private val clock: Clock,
) : BackupRepository {

    override suspend fun createBackup(): String = db.withTransaction {
        val snapshot = BackupSnapshot(
            tasks = dao.tasks(), habits = dao.habits(), habitLogs = dao.habitLogs(), goals = dao.goals(),
            milestones = dao.milestones(), journal = dao.journal(), events = dao.events(), profiles = dao.profiles(),
            focusSessions = dao.focusSessions(), achievements = dao.unlockedAchievements(),
        )
        BackupJson.toJson(snapshot, clock.millis())
    }

    override fun inspect(json: String): BackupCounts {
        val s = BackupJson.parse(json)
        return BackupCounts(s.tasks.size, s.habits.size, s.goals.size, s.journal.size, s.events.size)
    }

    override suspend fun restore(json: String) {
        val s = BackupJson.parse(json) // throws before anything is touched
        db.withTransaction {
            dao.clearHabitLogs(); dao.clearHabits(); dao.clearMilestones(); dao.clearGoals()
            dao.clearTasks(); dao.clearJournal(); dao.clearEvents(); dao.clearProfiles()
            dao.clearFocusSessions(); dao.clearUnlockedAchievements()
            dao.insertHabits(s.habits); dao.insertHabitLogs(s.habitLogs)
            dao.insertGoals(s.goals); dao.insertMilestones(s.milestones)
            dao.insertTasks(s.tasks); dao.insertJournal(s.journal); dao.insertEvents(s.events); dao.insertProfiles(s.profiles)
            dao.insertFocusSessions(s.focusSessions); dao.insertUnlockedAchievements(s.achievements)
        }
    }
}
