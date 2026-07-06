package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.HomeViewModel
import com.meshack.personalmanager.ui.PomodoroViewModel
import com.meshack.personalmanager.ui.RoutineViewModel
import com.meshack.personalmanager.ui.TasksViewModel

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    routineViewModel: RoutineViewModel = hiltViewModel(),
    tasksViewModel: TasksViewModel = hiltViewModel(),
    pomodoroViewModel: PomodoroViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val activeBlock by routineViewModel.currentActiveBlock.collectAsState(initial = null)
    val dashboard by homeViewModel.dashboard.collectAsState(initial = HomeViewModel.Dashboard())
    val tasks by tasksViewModel.tasks.collectAsState(initial = emptyList())
    val remaining = pomodoroViewModel.remainingSeconds

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active Block", style = MaterialTheme.typography.titleMedium)
                    Text(activeBlock?.name ?: "No active block", style = MaterialTheme.typography.bodyLarge)
                    Text("Completion", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = dashboard.completionPercent,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text(
                        "Today ${"%.0f".format(dashboard.completionPercent * 100)}% — ${dashboard.tasksDone}/${dashboard.tasksToday} tasks",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onNavigate(Screen.Routine) }, modifier = Modifier.weight(1f)) {
                    Text("Routine")
                }
                Button(onClick = { onNavigate(Screen.Tasks) }, modifier = Modifier.weight(1f)) {
                    Text("Tasks")
                }
                Button(onClick = { onNavigate(Screen.AppLimits) }, modifier = Modifier.weight(1f)) {
                    Text("Blocking")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modicalign = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Focus Mode", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Button(onClick = { pomodoroViewModel.toggle() }) {
                            Text(if (pomodoroViewModel.isRunning) "Pause" else "Start")
                        }
                    }
                    Text("Phase: ${pomodoroViewModel.phase.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Next in: ${remaining / 60}m ${remaining % 60}s", style = MaterialTheme.typography.bodyLarge)
                    if (pomodoroViewModel.completedCycles > 0) {
                        Text("Completed cycles: ${pomodoroViewModel.completedCycles}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
