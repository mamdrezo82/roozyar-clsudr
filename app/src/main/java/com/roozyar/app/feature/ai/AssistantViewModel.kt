package com.roozyar.app.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.ai.ActionRegistry
import com.roozyar.app.domain.ai.AssistantIntent
import com.roozyar.app.domain.ai.RuleBasedParser
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.ReportPeriod
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.buildReport
import com.roozyar.app.domain.model.isOverdue
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.JournalRepository
import com.roozyar.app.domain.usecase.GetTaskUseCase
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SaveTaskUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
import com.roozyar.app.feature.dashboard.summarizeToday
import com.roozyar.app.feature.planner.movedDueAt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class PendingAction(val summary: String, val actionTitle: String)

data class ChatMessage(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val pending: PendingAction? = null,
    val resolved: Boolean = false,
)

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val busy: Boolean = false,
)

private class Outcome(val success: Boolean, val message: String)

private const val MAX_LISTED = 6

/**
 * Chat with the on-device command assistant. The engine here is the rule-based parser; a local language model will
 * later produce the same [AssistantIntent] values. Whatever the engine, every change goes through the allow-list
 * ([ActionRegistry]), waits for the user's approval, and is verified against the database afterwards.
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val getTask: GetTaskUseCase,
    private val saveTask: SaveTaskUseCase,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val goals: GoalRepository,
    private val habits: HabitRepository,
    private val focus: FocusRepository,
    private val journal: JournalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private var nextId = 1L
    private val pendingRuns = mutableMapOf<Long, suspend () -> Outcome>()

    fun onInput(v: String) = _uiState.update { it.copy(input = v) }

    fun send(text: String = _uiState.value.input) {
        val t = text.trim()
        if (t.isEmpty() || _uiState.value.busy) return
        append(ChatMessage(nextId++, fromUser = true, text = t))
        _uiState.update { it.copy(input = "", busy = true) }
        viewModelScope.launch {
            try {
                handle(RuleBasedParser.parse(t, LocalDate.now(clock)))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                reply("مشکلی پیش آمد و درخواست انجام نشد.")
            } finally {
                _uiState.update { it.copy(busy = false) }
            }
        }
    }

    fun approve(messageId: Long) {
        val run = pendingRuns.remove(messageId) ?: return
        resolve(messageId)
        viewModelScope.launch {
            try {
                val outcome = run()
                reply(outcome.message)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                reply("اجرای عملیات ناموفق بود. چیزی تغییر نکرده است یا باید دستی بررسی شود.")
            }
        }
    }

    fun cancel(messageId: Long) {
        pendingRuns.remove(messageId)
        resolve(messageId)
        reply("لغو شد؛ تغییری اعمال نشد.")
    }

    private fun append(m: ChatMessage) = _uiState.update { it.copy(messages = it.messages + m) }

    private fun reply(text: String) = append(ChatMessage(nextId++, fromUser = false, text = text))

    private fun resolve(id: Long) = _uiState.update { s -> s.copy(messages = s.messages.map { if (it.id == id) it.copy(resolved = true) else it }) }

    private fun propose(summary: String, spec: com.roozyar.app.domain.ai.ActionSpec, run: suspend () -> Outcome) {
        val id = nextId++
        pendingRuns[id] = run
        append(ChatMessage(id, fromUser = false, text = "می‌خواهم این کار را انجام بدهم:", pending = PendingAction(summary, spec.title)))
    }

    private suspend fun handle(intent: AssistantIntent) {
        val today = LocalDate.now(clock)
        val spec = ActionRegistry.specFor(intent)
        when (intent) {
            AssistantIntent.Help -> reply(helpText())
            AssistantIntent.Unknown -> reply("متوجه نشدم. این‌ها را امتحان کنید:\n" + examples.joinToString("\n") { "• $it" })
            AssistantIntent.TodayTasks -> reply(todayTasksText())
            AssistantIntent.OverdueTasks -> reply(overdueText())
            AssistantIntent.GoalsStatus -> reply(goalsText())
            AssistantIntent.WeekReport -> reply(weekReportText())
            is AssistantIntent.CreateTask -> propose(
                "ثبت وظیفه «${intent.title}»" + (intent.dueDate?.let { " با مهلت ${dateLabel(it)}" } ?: " بدون مهلت"), spec!!,
            ) { createTask(intent, today) }
            is AssistantIntent.CreateJournal -> propose("ثبت یادداشت: «${intent.body.take(80)}»", spec!!) { createJournal(intent) }
            is AssistantIntent.MoveTask -> {
                val match = findOpenTask(intent.titleQuery)
                when (match) {
                    is Match.None -> reply("وظیفه باز با عنوان شبیه «${intent.titleQuery}» پیدا نشد.")
                    is Match.Many -> reply("چند وظیفه با این نام هست؛ دقیق‌تر بگویید:\n" + match.titles.joinToString("\n") { "• $it" })
                    is Match.One -> propose("انتقال «${match.task.title}» به ${dateLabel(intent.date)}", spec!!) { moveTask(match.task, intent.date) }
                }
            }
            is AssistantIntent.CompleteTask -> {
                val match = findOpenTask(intent.titleQuery)
                when (match) {
                    is Match.None -> reply("وظیفه باز با عنوان شبیه «${intent.titleQuery}» پیدا نشد.")
                    is Match.Many -> reply("چند وظیفه با این نام هست؛ دقیق‌تر بگویید:\n" + match.titles.joinToString("\n") { "• $it" })
                    is Match.One -> propose("علامت‌زدن «${match.task.title}» به‌عنوان انجام‌شده", spec!!) { completeTask(match.task) }
                }
            }
        }
    }

    // ---------- reads (nothing is changed) ----------

    private suspend fun todayTasksText(): String {
        val now = clock.instant()
        val s = summarizeToday(observeTasks().first(), now, clock.zone)
        if (s.todayOpen.isEmpty() && s.overdueCount == 0) return if (s.totalToday == 0) "امروز وظیفه‌ای ثبت نشده است." else "همه وظایف امروز انجام شده‌اند."
        val lines = s.todayOpen.take(MAX_LISTED).mapIndexed { i, t -> "${i + 1}. ${t.title}" }
        val more = (s.todayOpen.size - MAX_LISTED).coerceAtLeast(0)
        return buildString {
            append("امروز ${s.todayOpen.size} وظیفه باز داری:\n").append(lines.joinToString("\n"))
            if (more > 0) append("\nو $more مورد دیگر")
            if (s.overdueCount > 0) append("\n\n${s.overdueCount} وظیفه هم عقب‌افتاده است.")
        }.toPersianDigits()
    }

    private suspend fun overdueText(): String {
        val now = clock.instant()
        val overdue = observeTasks().first().filter { it.isOverdue(now, clock.zone) }.sortedBy { it.dueAt }
        if (overdue.isEmpty()) return "وظیفه عقب‌افتاده‌ای ندارید."
        val lines = overdue.take(MAX_LISTED).mapIndexed { i, t -> "${i + 1}. ${t.title}" }
        return ("${overdue.size} وظیفه عقب‌افتاده:\n" + lines.joinToString("\n") +
            if (overdue.size > MAX_LISTED) "\nو ${overdue.size - MAX_LISTED} مورد دیگر" else "").toPersianDigits()
    }

    private suspend fun goalsText(): String {
        val active = goals.observeGoals().first().filter { !it.completed }
        if (active.isEmpty()) return "هدف فعالی ندارید."
        val today = LocalDate.now(clock)
        return ("اهداف فعال:\n" + active.take(MAX_LISTED).joinToString("\n") { g ->
            val pct = (g.progress * 100).toInt()
            val left = g.daysRemaining(today)?.let { d -> if (d >= 0) " — $d روز مانده" else " — ${-d} روز گذشته از مهلت" } ?: ""
            "• ${g.title}: ٪$pct$left"
        }).toPersianDigits()
    }

    private suspend fun weekReportText(): String {
        val today = LocalDate.now(clock)
        val logs = habits.observeLogsFrom(today.minusDays(70)).first()
        val r = buildReport(
            ReportPeriod.WEEK, observeTasks().first(), habits.observeHabits().first(), logs,
            focus.observeSessions().first(), journal.observeEntries().first(), today, clock.zone,
        )
        val c = r.current
        return buildString {
            append("گزارش ۷ روز اخیر:\n")
            append("• وظیفه انجام‌شده: ${c.tasksDone}\n")
            append("• تمرکز: ${c.focusMinutes} دقیقه\n")
            append("• یادداشت: ${c.journalEntries}\n")
            append("• پایبندی عادت‌ها: ${c.habitAdherence?.let { "٪$it" } ?: "—"}")
            if (r.insights.isNotEmpty()) append("\n\n" + r.insights.joinToString("\n") { "– $it" })
        }.toPersianDigits()
    }

    // ---------- changes (only after approval; each verifies the result in the database) ----------

    private suspend fun createTask(i: AssistantIntent.CreateTask, today: LocalDate): Outcome {
        val now = clock.instant()
        val id = saveTask(
            Task(
                id = 0, title = i.title, description = "", dueAt = i.dueDate?.atStartOfDay(clock.zone)?.toInstant(), dueHasTime = false,
                priority = TaskPriority.NORMAL, status = TaskStatus.OPEN, categoryId = null, goalId = null, createdAt = now, updatedAt = now,
            ),
        )
        val stored = getTask(id)
        val ok = stored != null && stored.title == i.title && stored.status == TaskStatus.OPEN &&
            stored.dueAt?.atZone(clock.zone)?.toLocalDate() == i.dueDate
        return if (ok) Outcome(true, "وظیفه «${i.title}» ثبت شد.") else Outcome(false, "ثبت وظیفه تأیید نشد؛ لطفاً لیست وظایف را بررسی کنید.")
    }

    private suspend fun createJournal(i: AssistantIntent.CreateJournal): Outcome {
        val now = clock.instant()
        val id = journal.saveEntry(JournalEntry(0, "", i.body, null, emptyList(), false, false, now, now))
        val stored = journal.getEntry(id)
        return if (stored != null && stored.body == i.body) Outcome(true, "یادداشت ثبت شد.") else Outcome(false, "ثبت یادداشت تأیید نشد.")
    }

    private suspend fun moveTask(task: Task, date: LocalDate): Outcome {
        saveTask(task.copy(dueAt = movedDueAt(task, date, clock.zone)))
        val stored = getTask(task.id)
        val ok = stored?.dueAt?.atZone(clock.zone)?.toLocalDate() == date
        return if (ok) Outcome(true, "«${task.title}» به ${dateLabel(date)} منتقل شد.") else Outcome(false, "انتقال تأیید نشد.")
    }

    private suspend fun completeTask(task: Task): Outcome {
        setTaskStatus(task.id, TaskStatus.DONE)
        val ok = getTask(task.id)?.status == TaskStatus.DONE
        return if (ok) Outcome(true, "«${task.title}» انجام‌شده ثبت شد.") else Outcome(false, "تغییر وضعیت تأیید نشد.")
    }

    // ---------- helpers ----------

    private sealed interface Match {
        data object None : Match
        data class One(val task: Task) : Match
        data class Many(val titles: List<String>) : Match
    }

    /** Open tasks whose normalised title contains the query; an exact title wins over partial matches. */
    private suspend fun findOpenTask(query: String): Match {
        val q = RuleBasedParser.normalize(query)
        val open = observeTasks().first().filter { it.status == TaskStatus.OPEN }
        val exact = open.filter { RuleBasedParser.normalize(it.title) == q }
        val hits = exact.ifEmpty { open.filter { RuleBasedParser.normalize(it.title).contains(q) } }
        return when (hits.size) {
            0 -> Match.None
            1 -> Match.One(hits.first())
            else -> Match.Many(hits.take(3).map { it.title })
        }
    }

    private fun dateLabel(date: LocalDate): String {
        val j = date.toJalali()
        return "${JalaliFormatter.weekdayName(date.dayOfWeek)} ${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)}"
    }

    private fun helpText(): String = "این‌ها را می‌توانم انجام بدهم:\n" +
        ActionRegistry.allowed.joinToString("\n") { "• ${it.title}" + if (it.requiresApproval) " (با تأیید شما)" else "" }

    companion object {
        val examples = listOf(
            "امروز چه کارهایی دارم؟",
            "کارهای عقب‌افتاده‌ام را نشان بده",
            "یک وظیفه جدید ثبت کن: خرید نان برای فردا",
            "ارسال گزارش را برای فردا منتقل کن",
            "وضعیت اهدافم را بررسی کن",
            "گزارش هفته را آماده کن",
        )
    }
}
