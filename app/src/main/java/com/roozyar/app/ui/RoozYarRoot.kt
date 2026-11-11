package com.roozyar.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roozyar.feature.dashboard.DashboardScreen

private data class MainDestination(val route: String, val label: String)
private val destinations = listOf(
    MainDestination("home", "خانه"),
    MainDestination("planner", "برنامه‌ریز"),
    MainDestination("calendar", "تقویم"),
    MainDestination("goals", "اهداف"),
    MainDestination("profile", "پروفایل"),
)

object RoozYarRoutes {
    const val HOME = "home"
    const val PLANNER = "planner"
    const val CALENDAR = "calendar"
    const val GOALS = "goals"
    const val PROFILE = "profile"
    // Reserved from the first release so Tasks can become an independent feature in phase two.
    const val TASKS = "tasks"
}

@Composable
fun RoozYarRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (destination.route) {
                                "home" -> Icons.Outlined.Home
                                "planner" -> Icons.Outlined.Today
                                "calendar" -> Icons.Outlined.CalendarMonth
                                "goals" -> Icons.Outlined.Flag
                                else -> Icons.Outlined.Person
                            }
                            Icon(icon, contentDescription = destination.label)
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RoozYarRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(RoozYarRoutes.HOME) { DashboardScreen() }
            composable(RoozYarRoutes.PLANNER) { PlaceholderScreen("برنامه‌ریز هوشمند") }
            composable(RoozYarRoutes.CALENDAR) { PlaceholderScreen("تقویم شمسی") }
            composable(RoozYarRoutes.GOALS) { PlaceholderScreen("اهداف و مراحل") }
            composable(RoozYarRoutes.PROFILE) { PlaceholderScreen("پروفایل و تنظیمات") }
            composable(RoozYarRoutes.TASKS) { PlaceholderScreen("مدیریت وظایف") }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(PaddingValues(horizontal = 20.dp, vertical = 24.dp)),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text("این بخش در مرحله مربوط به خودش پیاده‌سازی خواهد شد.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
