package com.roozyar.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.roozyar.app.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

private val Background = Color(0xFF0B1326)
private val Primary = Color(0xFF4FDBC8)
private val OnSurface = Color(0xFFDAE2FD)
private val Muted = Color(0xFFBBCAC6)
private val Error = Color(0xFFFFB4AB)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun taskRepository(): TaskRepository
    fun habitRepository(): HabitRepository
    fun clock(): Clock
}

class RoozYarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val clock = ep.clock()
        val today = LocalDate.now(clock)
        val data = try {
            buildWidgetData(
                tasks = ep.taskRepository().observeTasks().first(),
                habits = ep.habitRepository().observeHabits().first(),
                logs = ep.habitRepository().observeLogsFrom(today.minusDays(400)).first(),
                now = clock.instant(),
                zone = clock.zone,
            )
        } catch (e: Exception) {
            null
        }
        provideContent { WidgetContent(data) }
    }
}

class RoozYarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RoozYarWidget()
}

@androidx.compose.runtime.Composable
private fun WidgetContent(d: WidgetData?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Background))
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (d == null) {
            Text("روزیار", style = TextStyle(color = ColorProvider(Primary), fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text("خواندن اطلاعات ممکن نشد", style = TextStyle(color = ColorProvider(Error), fontSize = 12.sp))
            return@Column
        }
        Text("روزیار • ${d.dateLabel}", style = TextStyle(color = ColorProvider(Primary), fontSize = 12.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        Spacer(GlanceModifier.height(6.dp))
        Text(d.tasksLine, style = TextStyle(color = ColorProvider(OnSurface), fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        d.tasks.forEach { title ->
            Text("• $title", style = TextStyle(color = ColorProvider(OnSurface), fontSize = 13.sp), maxLines = 1)
        }
        if (d.moreCount > 0) {
            Text("و ${d.moreCount} مورد دیگر", style = TextStyle(color = ColorProvider(Muted), fontSize = 11.sp), maxLines = 1)
        }
        Spacer(GlanceModifier.height(6.dp))
        d.habitsLine?.let { Text(it, style = TextStyle(color = ColorProvider(Primary), fontSize = 12.sp), maxLines = 1) }
        d.overdueLine?.let { Text(it, style = TextStyle(color = ColorProvider(Error), fontSize = 12.sp), maxLines = 1) }
    }
}
