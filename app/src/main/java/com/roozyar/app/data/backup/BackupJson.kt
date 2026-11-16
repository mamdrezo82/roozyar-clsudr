package com.roozyar.app.data.backup

import com.roozyar.app.core.database.EventEntity
import com.roozyar.app.core.database.GoalEntity
import com.roozyar.app.core.database.HabitEntity
import com.roozyar.app.core.database.HabitLogEntity
import com.roozyar.app.core.database.JournalEntryEntity
import com.roozyar.app.core.database.MilestoneEntity
import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.core.database.UserProfileEntity
import com.roozyar.app.domain.repository.BackupFormatException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Format of the backup file. Bump [VERSION] only with a migration path for older files. */
object BackupJson {
    const val APP = "RoozYar"
    const val VERSION = 1

    fun toJson(s: BackupSnapshot, exportedAtEpochMillis: Long): String = JSONObject().apply {
        put("app", APP)
        put("version", VERSION)
        put("exportedAt", exportedAtEpochMillis)
        put("tasks", array(s.tasks) { t ->
            JSONObject().put("id", t.id).put("title", t.title).put("description", t.description)
                .putN("dueAt", t.dueAtEpochMillis).put("dueHasTime", t.dueHasTime).put("priority", t.priority)
                .put("status", t.status).putN("categoryId", t.categoryId).putN("goalId", t.goalId)
                .put("createdAt", t.createdAtEpochMillis).put("updatedAt", t.updatedAtEpochMillis)
                .put("category", t.category).putN("reminderMinutes", t.reminderMinutesBefore)
        })
        put("habits", array(s.habits) { h ->
            JSONObject().put("id", h.id).put("title", h.title).put("icon", h.icon).put("weekdaysMask", h.weekdaysMask)
                .put("targetCount", h.targetCount).put("unit", h.unit).put("createdAt", h.createdAtEpochMillis)
                .putN("reminderMinuteOfDay", h.reminderMinuteOfDay)
        })
        put("habitLogs", array(s.habitLogs) { l ->
            JSONObject().put("id", l.id).put("habitId", l.habitId).put("epochDay", l.epochDay).put("count", l.count)
        })
        put("goals", array(s.goals) { g ->
            JSONObject().put("id", g.id).put("title", g.title).put("description", g.description).put("category", g.category)
                .put("priority", g.priority).putN("deadlineEpochDay", g.deadlineEpochDay).put("completed", g.completed)
                .putN("completedAt", g.completedAtEpochMillis).put("createdAt", g.createdAtEpochMillis)
        })
        put("milestones", array(s.milestones) { m ->
            JSONObject().put("id", m.id).put("goalId", m.goalId).put("title", m.title).put("done", m.done).put("position", m.position)
        })
        put("journal", array(s.journal) { j ->
            JSONObject().put("id", j.id).put("title", j.title).put("body", j.body).put("mood", j.mood).put("tags", j.tags)
                .put("starred", j.starred).put("isDraft", j.isDraft).put("createdAt", j.createdAtEpochMillis).put("updatedAt", j.updatedAtEpochMillis)
        })
        put("events", array(s.events) { e ->
            JSONObject().put("id", e.id).put("title", e.title).put("start", e.startEpochMillis).putN("end", e.endEpochMillis)
                .put("hasTime", e.hasTime).put("category", e.category).put("location", e.location)
        })
        put("profiles", array(s.profiles) { p ->
            JSONObject().put("id", p.id).put("displayName", p.displayName).put("bio", p.bio).putN("email", p.email).putN("phone", p.phone)
                .put("showEmail", p.showEmailInProfile).put("showPhone", p.showPhoneInProfile)
        })
    }.toString(2)

    /** @throws BackupFormatException for malformed files, wrong app/version, or broken references. */
    fun parse(json: String): BackupSnapshot {
        try {
            val root = JSONObject(json)
            if (root.optString("app") != APP) throw BackupFormatException("این فایل پشتیبان روزیار نیست.")
            val version = root.optInt("version", -1)
            if (version < 1) throw BackupFormatException("نسخه فایل پشتیبان نامعتبر است.")
            if (version > VERSION) throw BackupFormatException("این پشتیبان با نسخه جدیدتری از برنامه ساخته شده است؛ ابتدا برنامه را به‌روزرسانی کنید.")

            val snapshot = BackupSnapshot(
                tasks = list(root, "tasks") { o ->
                    TaskEntity(
                        id = o.getLong("id"), title = o.getString("title"), description = o.getString("description"),
                        dueAtEpochMillis = o.nLong("dueAt"), dueHasTime = o.getBoolean("dueHasTime"), priority = o.getInt("priority"),
                        status = o.getString("status"), categoryId = o.nLong("categoryId"), goalId = o.nLong("goalId"),
                        createdAtEpochMillis = o.getLong("createdAt"), updatedAtEpochMillis = o.getLong("updatedAt"),
                        category = o.optString("category", "NONE"), reminderMinutesBefore = o.nInt("reminderMinutes"),
                    )
                },
                habits = list(root, "habits") { o ->
                    HabitEntity(
                        id = o.getLong("id"), title = o.getString("title"), icon = o.getString("icon"),
                        weekdaysMask = o.getInt("weekdaysMask"), targetCount = o.getInt("targetCount"),
                        unit = o.getString("unit"), createdAtEpochMillis = o.getLong("createdAt"),
                        reminderMinuteOfDay = o.nInt("reminderMinuteOfDay"),
                    )
                },
                habitLogs = list(root, "habitLogs") { o ->
                    HabitLogEntity(o.getLong("id"), o.getLong("habitId"), o.getLong("epochDay"), o.getInt("count"))
                },
                goals = list(root, "goals") { o ->
                    GoalEntity(
                        id = o.getLong("id"), title = o.getString("title"), description = o.getString("description"),
                        category = o.getString("category"), priority = o.getInt("priority"), deadlineEpochDay = o.nLong("deadlineEpochDay"),
                        completed = o.getBoolean("completed"), completedAtEpochMillis = o.nLong("completedAt"),
                        createdAtEpochMillis = o.getLong("createdAt"),
                    )
                },
                milestones = list(root, "milestones") { o ->
                    MilestoneEntity(o.getLong("id"), o.getLong("goalId"), o.getString("title"), o.getBoolean("done"), o.getInt("position"))
                },
                journal = list(root, "journal") { o ->
                    JournalEntryEntity(
                        id = o.getLong("id"), title = o.getString("title"), body = o.getString("body"), mood = o.getString("mood"),
                        tags = o.getString("tags"), starred = o.getBoolean("starred"), isDraft = o.getBoolean("isDraft"),
                        createdAtEpochMillis = o.getLong("createdAt"), updatedAtEpochMillis = o.getLong("updatedAt"),
                    )
                },
                events = list(root, "events") { o ->
                    EventEntity(
                        id = o.getLong("id"), title = o.getString("title"), startEpochMillis = o.getLong("start"),
                        endEpochMillis = o.nLong("end"), hasTime = o.getBoolean("hasTime"),
                        category = o.getString("category"), location = o.getString("location"),
                    )
                },
                profiles = list(root, "profiles") { o ->
                    UserProfileEntity(
                        id = o.getInt("id"), displayName = o.getString("displayName"), bio = o.getString("bio"),
                        email = o.nString("email"), phone = o.nString("phone"),
                        showEmailInProfile = o.getBoolean("showEmail"), showPhoneInProfile = o.getBoolean("showPhone"),
                    )
                },
            )
            checkReferences(snapshot)
            return snapshot
        } catch (e: JSONException) {
            throw BackupFormatException("فایل پشتیبان معتبر یا کامل نیست.")
        }
    }

    private fun checkReferences(s: BackupSnapshot) {
        val habitIds = s.habits.map { it.id }.toSet()
        val goalIds = s.goals.map { it.id }.toSet()
        if (s.habitLogs.any { it.habitId !in habitIds }) throw BackupFormatException("فایل پشتیبان ناسازگار است (سابقه عادت بدون عادت).")
        if (s.milestones.any { it.goalId !in goalIds }) throw BackupFormatException("فایل پشتیبان ناسازگار است (مایلستون بدون هدف).")
    }

    private fun <T> array(items: List<T>, f: (T) -> JSONObject): JSONArray = JSONArray().also { a -> items.forEach { a.put(f(it)) } }

    private fun <T> list(root: JSONObject, key: String, f: (JSONObject) -> T): List<T> {
        val a = root.optJSONArray(key) ?: return emptyList()
        return (0 until a.length()).map { f(a.getJSONObject(it)) }
    }

    private fun JSONObject.putN(key: String, value: Any?): JSONObject = put(key, value ?: JSONObject.NULL)

    private fun JSONObject.nLong(key: String): Long? = if (isNull(key)) null else getLong(key)

    private fun JSONObject.nInt(key: String): Int? = if (isNull(key)) null else getInt(key)

    private fun JSONObject.nString(key: String): String? = if (isNull(key)) null else getString(key)
}
