package com.roozyar.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.roozyar.app.feature.calendar.CalendarScreen
import com.roozyar.app.feature.calendar.EventEditScreen
import com.roozyar.app.feature.dashboard.DashboardScreen
import com.roozyar.app.feature.goals.GoalDetailScreen
import com.roozyar.app.feature.goals.GoalEditScreen
import com.roozyar.app.feature.goals.GoalsScreen
import com.roozyar.app.feature.habits.HabitEditScreen
import com.roozyar.app.feature.habits.HabitsScreen
import com.roozyar.app.feature.hub.HubScreen
import com.roozyar.app.feature.journal.JournalEditScreen
import com.roozyar.app.feature.journal.JournalScreen
import com.roozyar.app.feature.profile.ProfileEditScreen
import com.roozyar.app.feature.profile.ProfileScreen
import com.roozyar.app.feature.tasks.TaskEditScreen
import com.roozyar.app.feature.tasks.TasksScreen

object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val HUB = "hub"
    const val GOALS = "goals"
    const val PROFILE = "profile"
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit?taskId={taskId}"
    const val TASK_ID_ARG = "taskId"
    const val HABITS = "habits"
    const val HABIT_EDIT = "habit_edit?habitId={habitId}"
    const val HABIT_ID_ARG = "habitId"
    const val PROFILE_EDIT = "profile_edit"
    const val EVENT_EDIT = "event_edit?eventId={eventId}&date={date}"
    const val EVENT_ID_ARG = "eventId"
    const val EVENT_DATE_ARG = "date"
    const val JOURNAL = "journal"
    const val JOURNAL_EDIT = "journal_edit?entryId={entryId}"
    const val ENTRY_ID_ARG = "entryId"
    const val GOAL_DETAIL = "goal/{goalId}"
    const val GOAL_EDIT = "goal_edit?goalId={goalId}"
    const val GOAL_ID_ARG = "goalId"

    /** Pass null to create a new task. */
    fun taskEdit(id: Long?): String = "task_edit?taskId=${id ?: -1L}"

    fun habitEdit(id: Long?): String = "habit_edit?habitId=${id ?: -1L}"

    fun eventEdit(id: Long?, date: java.time.LocalDate? = null): String =
        "event_edit?eventId=${id ?: -1L}&date=${date?.toEpochDay() ?: -1L}"

    fun journalEdit(id: Long?): String = "journal_edit?entryId=${id ?: -1L}"

    fun goalDetail(id: Long): String = "goal/$id"

    fun goalEdit(id: Long?): String = "goal_edit?goalId=${id ?: -1L}"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

// Order is logical (first item sits on the right in RTL): Home, Calendar, Hub, Goals, Profile.
private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "خانه", Icons.Outlined.Home),
    TopLevelDestination(Routes.CALENDAR, "تقویم", Icons.Outlined.CalendarMonth),
    TopLevelDestination(Routes.HUB, "برنامه‌ها", Icons.Outlined.GridView),
    TopLevelDestination(Routes.GOALS, "اهداف", Icons.Outlined.Flag),
    TopLevelDestination(Routes.PROFILE, "پروفایل", Icons.Outlined.Person),
)

/** Screens opened from the Hub keep the Hub tab highlighted. */
private val hubChildren = setOf(Routes.TASKS, Routes.HABITS, Routes.JOURNAL)

/** Full-screen forms hide the bottom bar. */
private val formPrefixes = listOf("task_edit", "habit_edit", "goal_edit", "journal_edit", "event_edit", "profile_edit")

private fun isTabSelected(tabRoute: String, currentRoute: String?): Boolean = when (tabRoute) {
    Routes.HUB -> currentRoute in hubChildren
    Routes.PROFILE -> currentRoute == Routes.PROFILE || currentRoute == Routes.PROFILE_EDIT
    Routes.GOALS -> currentRoute != null && currentRoute.startsWith("goal")
    Routes.CALENDAR -> currentRoute != null && (currentRoute == Routes.CALENDAR || currentRoute.startsWith("event"))
    else -> currentRoute == tabRoute
}

@Composable
fun RoozYarRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = formPrefixes.none { currentRoute?.startsWith(it) == true }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                val scheme = MaterialTheme.colorScheme
                NavigationBar(containerColor = scheme.surfaceContainerLow) {
                    topLevelDestinations.forEach { destination ->
                        val selected = isTabSelected(destination.route, currentRoute)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (destination.route == Routes.HOME) {
                                    // Home is the root of the stack: just go back to it, whatever is above.
                                    navController.popBackStack(Routes.HOME, inclusive = false)
                                } else {
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.HOME)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = scheme.primary,
                                selectedTextColor = scheme.primary,
                                indicatorColor = scheme.primaryContainer.copy(alpha = 0.28f),
                                unselectedIconColor = scheme.onSurfaceVariant,
                                unselectedTextColor = scheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                DashboardScreen(
                    onAddTask = { navController.navigate(Routes.taskEdit(null)) },
                    onAddEvent = { navController.navigate(Routes.eventEdit(null, java.time.LocalDate.now())) },
                    onAddJournal = { navController.navigate(Routes.journalEdit(null)) },
                    onOpenTasks = { navController.navigate(Routes.TASKS) },
                    onEditTask = { navController.navigate(Routes.taskEdit(it)) },
                    onOpenHabits = { navController.navigate(Routes.HABITS) },
                    onOpenGoals = { navController.navigate(Routes.GOALS) },
                    onOpenGoal = { navController.navigate(Routes.goalDetail(it)) },
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onAddEvent = { navController.navigate(Routes.eventEdit(null, it)) },
                    onEditEvent = { navController.navigate(Routes.eventEdit(it)) },
                    onEditTask = { navController.navigate(Routes.taskEdit(it)) },
                    onOpenGoal = { navController.navigate(Routes.goalDetail(it)) },
                    onOpenHabits = { navController.navigate(Routes.HABITS) },
                )
            }
            composable(
                route = Routes.EVENT_EDIT,
                arguments = listOf(
                    navArgument(Routes.EVENT_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Routes.EVENT_DATE_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                EventEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.HUB) { HubScreen(
                    onOpenTasks = { navController.navigate(Routes.TASKS) },
                    onOpenHabits = { navController.navigate(Routes.HABITS) },
                    onOpenJournal = { navController.navigate(Routes.JOURNAL) },
                ) }
            composable(Routes.GOALS) {
                GoalsScreen(
                    onAddGoal = { navController.navigate(Routes.goalEdit(null)) },
                    onOpenGoal = { navController.navigate(Routes.goalDetail(it)) },
                )
            }
            composable(
                route = Routes.GOAL_DETAIL,
                arguments = listOf(navArgument(Routes.GOAL_ID_ARG) { type = NavType.LongType }),
            ) {
                GoalDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.goalEdit(it)) },
                    onEditTask = { navController.navigate(Routes.taskEdit(it)) },
                )
            }
            composable(
                route = Routes.GOAL_EDIT,
                arguments = listOf(
                    navArgument(Routes.GOAL_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                GoalEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onEdit = { navController.navigate(Routes.PROFILE_EDIT) })
            }
            composable(Routes.PROFILE_EDIT) {
                ProfileEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    onBack = { navController.popBackStack() },
                    onAddTask = { navController.navigate(Routes.taskEdit(null)) },
                    onEditTask = { navController.navigate(Routes.taskEdit(it)) },
                )
            }
            composable(Routes.JOURNAL) {
                JournalScreen(
                    onBack = { navController.popBackStack() },
                    onAddEntry = { navController.navigate(Routes.journalEdit(null)) },
                    onOpenEntry = { navController.navigate(Routes.journalEdit(it)) },
                )
            }
            composable(
                route = Routes.JOURNAL_EDIT,
                arguments = listOf(
                    navArgument(Routes.ENTRY_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                JournalEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Routes.HABITS) {
                HabitsScreen(
                    onBack = { navController.popBackStack() },
                    onAddHabit = { navController.navigate(Routes.habitEdit(null)) },
                    onEditHabit = { navController.navigate(Routes.habitEdit(it)) },
                )
            }
            composable(
                route = Routes.HABIT_EDIT,
                arguments = listOf(
                    navArgument(Routes.HABIT_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                HabitEditScreen(onDone = { navController.popBackStack() })
            }
            composable(
                route = Routes.TASK_EDIT,
                arguments = listOf(
                    navArgument(Routes.TASK_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                TaskEditScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
