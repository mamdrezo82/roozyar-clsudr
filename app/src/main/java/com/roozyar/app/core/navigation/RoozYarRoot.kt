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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roozyar.app.feature.dashboard.DashboardScreen
import com.roozyar.app.feature.placeholder.PlaceholderScreen

object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val HUB = "hub"
    const val GOALS = "goals"
    const val PROFILE = "profile"
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

@Composable
fun RoozYarRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) { DashboardScreen() }
            composable(Routes.CALENDAR) { PlaceholderScreen("تقویم شمسی") }
            composable(Routes.HUB) { PlaceholderScreen("برنامه‌ها") }
            composable(Routes.GOALS) { PlaceholderScreen("اهداف و مراحل") }
            composable(Routes.PROFILE) { PlaceholderScreen("پروفایل و تنظیمات") }
        }
    }
}
