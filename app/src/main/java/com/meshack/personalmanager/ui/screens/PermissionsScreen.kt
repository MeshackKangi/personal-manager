package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.PermissionsViewModel
import com.meshack.personalmanager.util.PermissionStatus

@Composable
fun PermissionsScreen(onNavigate: (Screen) -> Unit, viewModel: PermissionsViewModel = hiltViewModel()) {
    val state = viewModel.state

    Scaffold(topBar = { TopAppBar(title = { Text("Permission Health Check") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Status of all required permissions. Tap Open Settings to fix any red items.", style = MaterialTheme.typography.bodySmall) }
            listOf(
                "usageAccess" to state.usageAccess,
                "accessibility" to state.accessibility,
                "overlay" to state.overlay,
                "notifications" to state.notifications,
                "exactAlarm" to state.exactAlarm,
                "calendar" to state.calendar,
                "batteryOptimization" to state.batteryOptimization
            ).forEach { (label, status) ->
                item {
                    PermissionRow(label, status, onClick = { viewModel.refresh() })
                }
            }
            item { Button(onClick = { viewModel.refresh() }, modifier = Modifier.fillMaxWidth()) { Text("Refresh") } }
        }
    }
}

@Composable
fun PermissionRow(label: String, status: PermissionStatus, onClick: () -> Unit) {
    val color = when (status) { is PermissionStatus.Granted -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.error }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label.replace("_", " ").replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleSmall)
                Text(status.toString(), style = MaterialTheme.typography.bodySmall, color = color)
            }
            TextButton(onClick = onClick) { Text("Open Settings") }
        }
    }
}
