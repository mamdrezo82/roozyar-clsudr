package com.roozyar.app.domain.ai

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Understands a small, fixed set of Persian command patterns. It is deliberately simple and predictable: anything it
 * does not recognise becomes [AssistantIntent.Unknown] instead of being guessed.
 */
object RuleBasedParser {

    fun normalize(input: String): String = input
        .replace('ي', 'ی').replace('ك', 'ک')
        .replace(Regex("[\\u064B-\\u065F\\u0640]"), "") // diacritics and tatweel
        .replace('\u200c', ' ')
        .replace(Regex("[؟?!.،,؛]+\\s*$"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private val weekdays = mapOf(
        "شنبه" to DayOfWeek.SATURDAY,
        "یکشنبه" to DayOfWeek.SUNDAY,
        "دوشنبه" to DayOfWeek.MONDAY,
        "سه شنبه" to DayOfWeek.TUESDAY,
        "چهار شنبه" to DayOfWeek.WEDNESDAY,
        "پنج شنبه" to DayOfWeek.THURSDAY,
        "جمعه" to DayOfWeek.FRIDAY,
    )

    private val datePhrase = Regex(
        "(?:(?:برای|تا|در|به)\\s+)?(?:روز\\s+)?(پس ?فردا|فردا|امروز|یک ?شنبه|دو ?شنبه|سه ?شنبه|چهار ?شنبه|پنج ?شنبه|جمعه|شنبه)(?=\\s|$)",
    )

    /** Removes the first date phrase from [text] and returns the remaining text with the date it named. */
    fun extractDate(text: String, today: LocalDate): Pair<String, LocalDate?> {
        val m = datePhrase.find(text) ?: return text to null
        val word = m.groupValues[1].replace(" ", "")
        val date = when (word) {
            "امروز" -> today
            "فردا" -> today.plusDays(1)
            "پسفردا" -> today.plusDays(2)
            else -> {
                val target = weekdays.entries.first { it.key.replace(" ", "") == word }.value
                var d = today.plusDays(1)
                while (d.dayOfWeek != target) d = d.plusDays(1)
                d
            }
        }
        val rest = (text.removeRange(m.range)).replace(Regex("\\s+"), " ").trim()
        return rest to date
    }

    private fun cleanTitle(s: String): String = s.trim().trim('«', '»', '"', ':', '-', ' ').replace(Regex("\\s+"), " ").trim()

    private val createTask = listOf(
        Regex("^(?:یک )?(?:وظیفه|کار) جدید(?: ثبت کن| اضافه کن| بساز)? ?:? ?(.+)$"),
        Regex("^(?:یادم بنداز|یادآوری کن) ?:? ?(.+)$"),
        Regex("^(.+?) را (?:به )?(?:وظایف|لیست(?: کارها)?) اضافه کن$"),
        Regex("^(?:ثبت کن|اضافه کن) ?: ?(.+)$"),
    )
    private val createJournal = Regex("^(?:یک )?یادداشت(?: جدید)?(?: ثبت کن| بنویس| اضافه کن)? ?: ?(.+)$")
    private val moveTask = Regex("^(?:وظیفه )?(.+?) را (?:برای|به) (.+?) (?:منتقل|جابه ?جا) کن$")
    private val completeTask = listOf(
        Regex("^(?:وظیفه )?(.+?) (?:انجام شد|تمام شد|کامل شد)$"),
        Regex("^(.+?) را (?:انجام دادم|تمام کردم)$"),
    )

    fun parse(input: String, today: LocalDate): AssistantIntent {
        val text = normalize(input)
        if (text.isEmpty()) return AssistantIntent.Unknown

        createJournal.find(text)?.let { m ->
            val body = cleanTitle(m.groupValues[1])
            if (body.isNotEmpty()) return AssistantIntent.CreateJournal(body)
        }
        moveTask.find(text)?.let { m ->
            val (_, date) = extractDate(m.groupValues[2], today)
            val query = cleanTitle(m.groupValues[1])
            if (date != null && query.isNotEmpty()) return AssistantIntent.MoveTask(query, date)
        }
        for (r in createTask) {
            r.find(text)?.let { m ->
                val (rest, date) = extractDate(m.groupValues[1], today)
                val title = cleanTitle(rest)
                if (title.isNotEmpty()) return AssistantIntent.CreateTask(title, date)
            }
        }
        for (r in completeTask) {
            r.find(text)?.let { m ->
                val q = cleanTitle(m.groupValues[1])
                if (q.isNotEmpty()) return AssistantIntent.CompleteTask(q)
            }
        }

        return when {
            "عقب" in text -> AssistantIntent.OverdueTasks
            "گزارش" in text -> AssistantIntent.WeekReport
            ("هدف" in text || "اهداف" in text) && listOf("وضعیت", "بررسی", "چطور", "پیشرفت").any { it in text } -> AssistantIntent.GoalsStatus
            "امروز" in text && listOf("چه کار", "کارها", "وظایف", "وظیفه", "برنامه").any { it in text } -> AssistantIntent.TodayTasks
            listOf("کمک", "راهنما", "چه کاری", "چه کار می").any { it in text } -> AssistantIntent.Help
            else -> AssistantIntent.Unknown
        }
    }
}
