package com.roozyar.app.data

import com.roozyar.app.core.database.EventEntity
import com.roozyar.app.core.database.GoalEntity
import com.roozyar.app.core.database.HabitEntity
import com.roozyar.app.core.database.HabitLogEntity
import com.roozyar.app.core.database.JournalEntryEntity
import com.roozyar.app.core.database.MilestoneEntity
import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.core.database.UserProfileEntity
import com.roozyar.app.data.backup.BackupJson
import com.roozyar.app.data.backup.BackupSnapshot
import com.roozyar.app.domain.repository.BackupFormatException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupJsonTest {
    private val snapshot = BackupSnapshot(
        tasks = listOf(
            TaskEntity(1, "کار ۱", "توضیح\nچندخطی «نقل»", 1_700_000_000_000, true, 1, "OPEN", null, 5, 10, 20),
            TaskEntity(2, "بدون مهلت", "", null, false, 3, "DONE", null, null, 11, 21),
        ),
        habits = listOf(HabitEntity(1, "آب", "water", 127, 8, "لیوان", 5)),
        habitLogs = listOf(HabitLogEntity(1, 1, 20_000, 3)),
        goals = listOf(GoalEntity(5, "هدف", "d", "CAREER", 2, 20_500, false, null, 1)),
        milestones = listOf(MilestoneEntity(1, 5, "مرحله", true, 0)),
        journal = listOf(JournalEntryEntity(1, "t", "متن\nبا خط جدید", "GOOD", "کار,ایده", true, false, 1, 2)),
        events = listOf(
            EventEntity(1, "جلسه", 100, 200, true, "WORK", "دفتر"),
            EventEntity(2, "تمام روز", 300, null, false, "PERSONAL", ""),
        ),
        profiles = listOf(UserProfileEntity(1, "سهراب", "bio", "a@b.c", null, true, false)),
        focusSessions = listOf(
            com.roozyar.app.core.database.FocusSessionEntity(1, 500, 25, 1500, true, 1, null),
            com.roozyar.app.core.database.FocusSessionEntity(2, 900, 45, 600, false, null, 5),
        ),
        achievements = listOf(com.roozyar.app.core.database.UnlockedAchievementEntity("FIRST_TASK", 777)),
    )

    @Test
    fun roundTripPreservesEveryTable() {
        val parsed = BackupJson.parse(BackupJson.toJson(snapshot, 123))
        assertEquals(snapshot, parsed)
    }

    @Test
    fun emptyDatabaseRoundTrips() {
        assertEquals(BackupSnapshot(), BackupJson.parse(BackupJson.toJson(BackupSnapshot(), 1)))
    }

    @Test
    fun rejectsGarbageAndForeignFiles() {
        listOf("", "not json", "[]", """{"app":"Other","version":1}""").forEach { bad ->
            try {
                BackupJson.parse(bad)
                fail("accepted: $bad")
            } catch (e: BackupFormatException) {
                assertTrue(e.message!!.isNotBlank())
            }
        }
    }

    @Test
    fun rejectsNewerVersion() {
        try {
            BackupJson.parse("""{"app":"RoozYar","version":99}""")
            fail()
        } catch (e: BackupFormatException) {
            assertTrue(e.message!!.contains("جدیدتر"))
        }
    }

    @Test
    fun rejectsBrokenReferences() {
        val json = JSONObject(BackupJson.toJson(snapshot, 1)).also { it.getJSONArray("goals").remove(0) }.toString()
        try {
            BackupJson.parse(json)
            fail()
        } catch (e: BackupFormatException) {
            assertTrue(e.message!!.contains("ناسازگار"))
        }
    }

    @Test
    fun rejectsTruncatedRecords() {
        val json = JSONObject(BackupJson.toJson(snapshot, 1)).also { it.getJSONArray("tasks").getJSONObject(0).remove("title") }.toString()
        try {
            BackupJson.parse(json)
            fail()
        } catch (e: BackupFormatException) {
            assertTrue(e.message!!.isNotBlank())
        }
    }

    @Test
    fun missingTablesDefaultToEmpty() {
        val parsed = BackupJson.parse("""{"app":"RoozYar","version":1}""")
        assertEquals(BackupSnapshot(), parsed)
    }
}
