package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.Screen
import com.meshack.personalmanager.ui.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(onNavigate: (Screen) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    var newTitle by remember { mutableStateOf("") }
    if (viewModel.showNewTaskDialog) {
        AlertDialog(onDismissRequest = { viewModel.dismissNewTaskDialog() }, title = { Text("New Task") }, text = {
            OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") })
        }, confirmButton = {
            TextButton(onClick = { viewModel.addTask(newTitle); newTitle = "" }) { Text("Add") }
        }, dismissButton = { TextButton(onClick = { viewModel.dismissNewTaskDialog() }) { Text("Cancel") } })
    }

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Tasks") }) }, floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.openNewTaskDialog() }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Completion", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = viewModel.completionPercent,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${"%.0f".format(viewModel.completionPercent * 100)}%", style = MaterialTheme.typography.bodySmall)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks) { task ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, style = MaterialTheme.typography.titleSmall)
                            }
                            Checkbox(checked = task.done, onCheckedChange = { viewModel.toggleDone(task) })
                        }
                    }
                }
            }
        }
    }
}
