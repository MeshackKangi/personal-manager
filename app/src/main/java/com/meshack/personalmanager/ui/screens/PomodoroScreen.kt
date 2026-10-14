package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.PomodoroViewModel
import com.meshack.personalmanager.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: PomodoroViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(viewModel.phase.name, style = MaterialTheme.typography.titleLarge)
                    Text("${viewModel.remainingSeconds / 60}m ${viewModel.remainingSeconds % 60}s remaining", style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = { viewModel.toggle() }) { Text(if (viewModel.isRunning) "Pause" else "Start") }
                    TextButton(onClick = { viewModel.reset() }) { Text("Reset") }
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
