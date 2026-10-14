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
import com.meshack.personalmanager.ui.Screen
import com.meshack.personalmanager.ui.SettingsViewModel
import com.meshack.personalmanager.ui.components.TimePickerField
import com.meshack.personalmanager.ui.components.formatMinute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: RoutineViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val blocks by viewModel.blocks.collectAsState(initial = emptyList())
    val error = viewModel.error
    val use24h by settingsViewModel.use24h.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(contentPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        onClick = { viewModel.onDayTypeChanged(DayType.WEEKDAY) },
                        selected = viewModel.dayType == DayType.WEEKDAY,
                        label = { Text("Weekday") }
                    )

                    FilterChip(
                        onClick = { viewModel.onDayTypeChanged(DayType.WEEKEND) },
                        selected = viewModel.dayType == DayType.WEEKEND,
                        label = { Text("Weekend") }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Add Block")
                    }
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
                            text = "${formatMinute(block.startMinute, use24h)} - ${formatMinute(block.endMinute, use24h)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(if (block.isFreeTime) "Free time" else "Focus", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

    if (viewModel.showAddDialog) {
        AddRoutineBlockDialog(
            use24h = use24h,
            onSave = { name, startMinute, endMinute, free ->
                viewModel.insert(RoutineBlock(name = name, dayType = viewModel.dayType, startMinute = startMinute, endMinute = endMinute, isFreeTime = free))
            },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }
}

@Composable
fun AddRoutineBlockDialog(
    use24h: Boolean,
    onSave: (String, Int, Int, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(8 * 60) }
    var end by remember { mutableStateOf(9 * 60) }
    var isFree by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Block") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            TimePickerField(label = "Start", minuteOfDay = start, use24h = use24h, onPick = { start = it })
            TimePickerField(label = "End", minuteOfDay = end, use24h = use24h, onPick = { end = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                Text("Free time")
            }
        }
    }, confirmButton = {
        TextButton(
            onClick = {
                if (name.isBlank() || start !in 0..1439 || end !in 0..1439 || end <= start) return@TextButton
                onSave(name.trim(), start, end, isFree)
            },
            enabled = name.isNotBlank() && end > start
        ) { Text("Save") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
