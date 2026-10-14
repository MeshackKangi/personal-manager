package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.data.local.entities.Milestone
import com.meshack.personalmanager.data.local.entities.Task
import com.meshack.personalmanager.domain.MilestonePriority
import com.meshack.personalmanager.ui.GoalsViewModel
import com.meshack.personalmanager.ui.theme.GoalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onNavigateUp: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val summaries by viewModel.summaries.collectAsState()
    val milestones by viewModel.milestones(goalId).collectAsState()
    val summary = summaries.firstOrNull { it.goal.id == goalId }
    var showAddMilestone by remember { mutableStateOf(false) }
    var showRenameGoal by remember { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf(false) }

    if (showAddMilestone) {
        EditMilestoneDialog(
            initial = null,
            onSave = { title, notes, priority ->
                viewModel.createMilestone(goalId, title, notes, priority)
                showAddMilestone = false
            },
            onDismiss = { showAddMilestone = false }
        )
    }
    if (showRenameGoal && summary != null) {
        RenameDialog(
            title = "Rename goal",
            initial = summary.goal.title,
            onSave = { viewModel.renameGoal(goalId, it); showRenameGoal = false },
            onDismiss = { showRenameGoal = false }
        )
    }
    if (confirmArchive) {
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            title = { Text("Archive goal?") },
            text = { Text("It leaves the dashboard; milestones and tasks stay saved.") },
            confirmButton = {
                TextButton(onClick = { confirmArchive = false; viewModel.archiveGoal(goalId); onNavigateUp() }) {
                    Text("Archive")
                }
            },
            dismissButton = { TextButton(onClick = { confirmArchive = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMilestone = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add milestone")
            }
        }
    ) { padding ->
        if (summary == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Goal not found — it may be archived.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateUp) { Text("Back to Goals") }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onNavigateUp) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Box(
                                modifier = Modifier.size(14.dp).clip(CircleShape)
                                    .background(GoalColors.forGoal(summary.goal.id))
                            )
                            Text(
                                summary.goal.title,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(onClick = { showRenameGoal = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename goal")
                            }
                        }
                        LinearProgressIndicator(
                            progress = summary.percent,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${(summary.percent * 100).toInt()}% · ${summary.doneTasks}/${summary.totalTasks} tasks",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { confirmArchive = true }) { Text("Archive") }
                        }
                    }
                }
            }
            if (milestones.isEmpty()) {
                item {
                    Text(
                        "No milestones yet — break this goal down with the + button.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(milestones, key = { it.id }) { milestone ->
                MilestoneCard(milestone = milestone, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MilestoneCard(milestone: Milestone, viewModel: GoalsViewModel) {
    val tasks by viewModel.tasksFor(milestone.id).collectAsState()
    var showAddTask by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    if (showEdit) {
        EditMilestoneDialog(
            initial = milestone,
            onSave = { title, notes, priority ->
                viewModel.updateMilestone(milestone.copy(title = title, notes = notes, priority = priority))
                showEdit = false
            },
            onDismiss = { showEdit = false }
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete milestone?") },
            text = { Text("Its tasks and subtasks go with it. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.deleteMilestone(milestone.id) }) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    milestone.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AssistChip(
                    onClick = {},
                    label = { Text(milestone.priority.name) },
                    enabled = false
                )
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit milestone", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete milestone", modifier = Modifier.size(20.dp))
                }
            }
            if (milestone.notes.isNotBlank()) {
                Text(milestone.notes, style = MaterialTheme.typography.bodySmall)
            }
            val total = tasks.size
            val done = tasks.count { it.done }
            if (total > 0) {
                LinearProgressIndicator(
                    progress = done.toFloat() / total,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text("$done/$total done", style = MaterialTheme.typography.bodySmall)
            }
            tasks.filter { it.parentTaskId == null }.forEach { task ->
                TaskRow(task = task, viewModel = viewModel, milestone = milestone)
            }
            if (showAddTask) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("New task") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        enabled = newTaskTitle.isNotBlank(),
                        onClick = { viewModel.addTask(milestone, newTaskTitle); newTaskTitle = ""; showAddTask = false }
                    ) { Text("Add") }
                }
            } else {
                TextButton(onClick = { showAddTask = true }) { Text("+ Add task") }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, viewModel: GoalsViewModel, milestone: Milestone) {
    var subtasks by remember(task.id, task.done) { mutableStateOf<List<Task>>(emptyList()) }
    var showSubField by remember { mutableStateOf(false) }
    var subTitle by remember { mutableStateOf("") }
    var showRename by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(task.id, task.done) {
        subtasks = viewModel.subtasksOf(task.id)
    }
    if (showRename) {
        RenameDialog(
            title = "Rename task",
            initial = task.title,
            onSave = { viewModel.renameTask(task, it); showRename = false },
            onDismiss = { showRename = false }
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = { Text("Its subtasks go with it.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.deleteTask(task) }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { viewModel.toggleTask(task) })
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = { showRename = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename task", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete task", modifier = Modifier.size(18.dp))
            }
        }
        subtasks.forEach { sub ->
            Row(
                modifier = Modifier.padding(start = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = sub.done, onCheckedChange = { viewModel.toggleTask(sub) })
                Text(
                    sub.title,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { viewModel.deleteTask(sub) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete subtask", modifier = Modifier.size(16.dp))
                }
            }
        }
        if (showSubField) {
            Row(
                modifier = Modifier.padding(start = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subTitle,
                    onValueChange = { subTitle = it },
                    label = { Text("Subtask") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    enabled = subTitle.isNotBlank(),
                    onClick = { viewModel.addSubtask(task, milestone, subTitle); subTitle = ""; showSubField = false }
                ) { Text("Add") }
            }
        } else {
            TextButton(onClick = { showSubField = true }, modifier = Modifier.padding(start = 32.dp)) {
                Text("+ Subtask", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EditMilestoneDialog(
    initial: Milestone?,
    onSave: (String, String, MilestonePriority) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var priority by remember { mutableStateOf(initial?.priority ?: MilestonePriority.MEDIUM) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New milestone" else "Edit milestone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MilestonePriority.values().forEach { option ->
                        androidx.compose.material3.FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), notes, priority) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RenameDialog(title: String, initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim()) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
