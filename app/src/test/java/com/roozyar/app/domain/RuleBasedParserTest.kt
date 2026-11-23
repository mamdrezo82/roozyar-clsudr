package com.roozyar.app.domain

import com.roozyar.app.domain.ai.ActionRegistry
import com.roozyar.app.domain.ai.AssistantIntent
import com.roozyar.app.domain.ai.RuleBasedParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RuleBasedParserTest {
    private val today = LocalDate.of(2026, 11, 11) // Wednesday
    private fun parse(s: String) = RuleBasedParser.parse(s, today)

    @Test
    fun readCommands() {
        assertEquals(AssistantIntent.TodayTasks, parse("امروز چه کارهایی دارم؟"))
        assertEquals(AssistantIntent.OverdueTasks, parse("کارهای عقب‌افتاده‌ام را نشان بده"))
        assertEquals(AssistantIntent.GoalsStatus, parse("وضعیت اهدافم را بررسی کن"))
        assertEquals(AssistantIntent.WeekReport, parse("گزارش هفته را آماده کن"))
        assertEquals(AssistantIntent.Help, parse("کمک"))
    }

    @Test
    fun createTaskWithAndWithoutDate() {
        assertEquals(AssistantIntent.CreateTask("خرید نان", today.plusDays(1)), parse("یک وظیفه جدید ثبت کن: خرید نان برای فردا"))
        assertEquals(AssistantIntent.CreateTask("تماس با بانک", null), parse("وظیفه جدید: تماس با بانک"))
        assertEquals(AssistantIntent.CreateTask("گزارش هفتگی", today), parse("یادم بنداز گزارش هفتگی امروز"))
        assertEquals(AssistantIntent.CreateTask("خرید شیر", null), parse("خرید شیر را به وظایف اضافه کن"))
    }

    @Test
    fun weekdayNamesResolveToTheNextOccurrence() {
        // today is Wednesday: Thursday = tomorrow, Wednesday = next week
        assertEquals(AssistantIntent.CreateTask("جلسه", today.plusDays(1)), parse("کار جدید: جلسه برای پنجشنبه"))
        assertEquals(AssistantIntent.CreateTask("جلسه", today.plusDays(7)), parse("کار جدید: جلسه برای چهارشنبه"))
        assertEquals(AssistantIntent.CreateTask("جلسه", today.plusDays(2)), parse("کار جدید: جلسه برای پس‌فردا"))
    }

    @Test
    fun journalNeedsContent() {
        assertEquals(AssistantIntent.CreateJournal("امروز روز خوبی بود"), parse("یک یادداشت جدید ثبت کن: امروز روز خوبی بود"))
        assertEquals(AssistantIntent.Unknown, parse("یادداشت جدید ثبت کن"))
    }

    @Test
    fun moveAndComplete() {
        assertEquals(AssistantIntent.MoveTask("ارسال گزارش", today.plusDays(1)), parse("ارسال گزارش را برای فردا منتقل کن"))
        assertEquals(AssistantIntent.MoveTask("ارسال گزارش", today.plusDays(1)), parse("وظیفه ارسال گزارش را به فردا جابه‌جا کن"))
        assertEquals(AssistantIntent.CompleteTask("ارسال گزارش"), parse("ارسال گزارش انجام شد"))
        assertEquals(AssistantIntent.CompleteTask("ارسال گزارش"), parse("ارسال گزارش را انجام دادم"))
    }

    @Test
    fun arabicLettersAndZwnjAreNormalised() {
        assertEquals(AssistantIntent.CreateTask("خرید نان", null), parse("وظيفه جديد: خرید نان".replace("وظيفه جديد", "وظیفه جدید")))
        assertEquals("کارهای عقب افتاده", RuleBasedParser.normalize("کارهای عقب\u200cافتاده؟"))
    }

    @Test
    fun unknownTextIsNotGuessed() {
        assertEquals(AssistantIntent.Unknown, parse("سلام"))
        assertEquals(AssistantIntent.Unknown, parse(""))
        assertEquals(AssistantIntent.Unknown, parse("حذف همه وظایف"))
    }

    @Test
    fun registryRequiresApprovalForEveryChange() {
        val write = listOf(
            AssistantIntent.CreateTask("x", null), AssistantIntent.CreateJournal("x"),
            AssistantIntent.MoveTask("x", today), AssistantIntent.CompleteTask("x"),
        )
        write.forEach { assertTrue(ActionRegistry.specFor(it)!!.requiresApproval) }
        listOf(AssistantIntent.TodayTasks, AssistantIntent.OverdueTasks, AssistantIntent.GoalsStatus, AssistantIntent.WeekReport)
            .forEach { assertFalse(ActionRegistry.specFor(it)!!.requiresApproval) }
        assertNull(ActionRegistry.specFor(AssistantIntent.Unknown))
        assertEquals(ActionRegistry.allowed.size, ActionRegistry.allowed.map { it.id }.toSet().size)
    }

    @Test
    fun extractDateKeepsTheRestOfTheText() {
        val (rest, date) = RuleBasedParser.extractDate("خرید نان فردا", today)
        assertEquals("خرید نان", rest)
        assertEquals(today.plusDays(1), date)
        assertNull(RuleBasedParser.extractDate("بدون تاریخ", today).second)
    }
}
