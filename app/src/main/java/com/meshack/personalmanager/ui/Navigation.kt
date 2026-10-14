package com.meshack.personalmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.meshack.personalmanager.ui.screens.AppLimitsScreen
import com.meshack.personalmanager.ui.screens.GoalDetailScreen
import com.meshack.personalmanager.ui.screens.GoalsScreen
import com.meshack.personalmanager.ui.screens.HomeScreen
import com.meshack.personalmanager.ui.screens.NotesScreen
import com.meshack.personalmanager.ui.screens.PermissionsScreen
import com.meshack.personalmanager.ui.screens.PomodoroScreen
import com.meshack.personalmanager.ui.screens.RoutineScreen
import com.meshack.personalmanager.ui.screens.SettingsScreen
import com.meshack.personalmanager.ui.screens.TasksScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Filled.Home)
    object Routine : Screen("routine", "Routine", Icons.Filled.Schedule)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.Done)
    object Goals : Screen("goals", "Goals", Icons.Filled.EmojiEvents)
    object AppLimits : Screen("app_limits", "Blocking", Icons.Filled.Block)
    object Pomodoro : Screen("pomodoro", "Focus", Icons.Filled.Timer)
    object Notes : Screen("notes", "Notes", Icons.Filled.Notes)
    object Permissions : Screen("permissions", "Permissions", Icons.Filled.Security)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        const val GOAL_DETAIL_BASE = "goal_detail"
        fun goalDetailRoute(goalId: Long) = "$GOAL_DETAIL_BASE/$goalId"

        val drawerItems: List<Screen> =
            listOf(Home, Routine, Tasks, Goals, AppLimits, Pomodoro, Notes, Permissions, Settings)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalManagerNavHost() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Screen.drawerItems.firstOrNull { it.route == currentRoute } ?: Screen.Home
    val topBarTitle = if (currentRoute?.startsWith(Screen.GOAL_DETAIL_BASE) == true) "Goal details"
    else currentScreen.title

    fun navigate(screen: Screen) {
        scope.launch { drawerState.close() }
        nav.navigate(screen.route) {
            launchSingleTop = true
            popUpTo(Screen.Home.route)
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Personal Manager",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Screen.drawerItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = { navigate(screen) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Routine.route) {
                    RoutineScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Tasks.route) {
                    TasksScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Goals.route) {
                    GoalsScreen(
                        onOpenGoal = { goalId ->
                            scope.launch { drawerState.close() }
                            nav.navigate(Screen.goalDetailRoute(goalId))
                        },
                        contentPadding = padding
                    )
                }
                composable(
                    route = "${Screen.GOAL_DETAIL_BASE}/{goalId}",
                    arguments = listOf(navArgument("goalId") { type = NavType.LongType })
                ) { entry ->
                    GoalDetailScreen(
                        goalId = entry.arguments?.getLong("goalId") ?: 0L,
                        onNavigateUp = { nav.popBackStack() },
                        contentPadding = padding
                    )
                }
                composable(Screen.AppLimits.route) {
                    AppLimitsScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Pomodoro.route) {
                    PomodoroScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Permissions.route) {
                    PermissionsScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Notes.route) {
                    NotesScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onNavigate = { navigate(it) }, contentPadding = padding)
                }
            }
        }
    }
}
