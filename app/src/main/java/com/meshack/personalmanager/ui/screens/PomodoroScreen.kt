package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.PomodoroViewModel

@Composable
fun PomodoroScreen(onNavigate: (Screen) -> Unit, viewModel: PomodoroViewModel = hiltViewModel()) {
    Scaffold(topBar = { TopAppBar(title = { Text("Focus Mode") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(viewModel.phase.name, style = MaterialTheme.typography.titleLarge)
                    Text("${viewModel.remainingSeconds / 60}m ${viewModel.remainingSeconds % 60}s remaining", style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = { viewModel.toggle() }) { Text(if (viewModel.isRunning) "Pause" else "Start") }
                    TextButton(onClick = { if (viewModel.phase == com.meshack.personalmanager.ui.PomodoroViewModel.Phase.WORK) viewModel.resetWorkPhase() else viewModel.resetBreakPhase() }) { Text("Reset") }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("History", style = MaterialTheme.typography.titleMedium)
                    Text("Cycles completed: ${viewModel.completedCycles}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
