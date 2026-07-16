package com.meshack.personalmanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meshack.personalmanager.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Routine : Screen("routine")
    object Tasks : Screen("tasks")
    object AppLimits : Screen("app_limits")
    object Pomodoro : Screen("pomodoro")
    object Permissions : Screen("permissions")
    object Notes : Screen("notes")
}

@Composable
fun PersonalManagerNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(onNavigate = { nav.navigate(it.route) }) }
        composable(Screen.Routine.route) { RoutineScreen(onNavigate = {} /* sealed nav back wiring omitted for brevity */ ) }
        composable(Screen.Tasks.route) { TasksScreen(onNavigate = {}) }
        composable(Screen.AppLimits.route) { AppLimitsScreen(onNavigate = {}) }
        composable(Screen.Pomodoro.route) { PomodoroScreen(onNavigate = {}) }
        composable(Screen.Permissions.route) { PermissionsScreen(onNavigate = {}) }
        composable(Screen.Notes.route) { NotesScreen(onNavigate = {}) }
    }
}
