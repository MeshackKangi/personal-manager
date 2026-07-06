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
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.domain.DayType
import com.meshack.personalmanager.ui.RoutineViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(onNavigate: (Screen) -> Unit, viewModel: RoutineViewModel = hiltViewModel()) {
    val blocks by viewModel.blocks.collectAsState(initial = emptyList())
    val error = viewModel.error
    var startMin by remember { mutableStateOf("") }
    var endMin by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Routine") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(onClick = { viewModel.onDayTypeChanged(DayType.WEEKDAY) }, selected = true, label = { Text("Weekday") })
                    FilterChip(onClick = { }, selected = false, label = { Text("Weekend") })
                }
            }
            item {
                OutlinedButton(onClick = { viewModel.openAddDialog() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Add Block")
                }
            }
            error?.let {
                item { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            items(blocks) { block ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(block.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${LocalTime.of(block.startMinute / 60, block.startMinute % 60)} - ${LocalTime.of(block.endMinute / 60, block.endMinute % 60)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(if (block.isFreeTime) "Free time" else "Focus", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        AddRoutineBlockDialog(
            onSave = { name, startMinute, endMinute, free ->
                viewModel.insert(RoutineBlock(name = name, dayType = DayType.WEEKDAY, startMinute = startMinute, endMinute = endMinute, isFreeTime = free))
            },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }
}

@Composable
fun AddRoutineBlockDialog(onSave: (String, Int, Int, Boolean) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var isFree by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Block") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start minute 0-1439") }, isError = start.toIntOrNull() == null && start.isNotBlank())
            OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End minute 0-1439") }, isError = end.toIntOrNull() == null && end.isNotBlank())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                Text("Free time")
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            val s = start.toIntOrNull() ?: return@TextButton
            val e = end.toIntOrNull() ?: return@TextButton
            if (s !in 0..1439 || e !in 0..1439) return@TextButton
            onSave(name.trim(), s, e, isFree)
        }) { Text("Save") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
