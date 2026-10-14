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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.AppLimitsViewModel
import com.meshack.personalmanager.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitsScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: AppLimitsViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val limits by viewModel.limits.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add App Limit / Blacklist", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = viewModel.newPackage, onValueChange = { viewModel.onPackageChanged(it) }, label = { Text("Package or App name") })
                    OutlinedTextField(value = viewModel.newLimitMinutes.toString(), onValueChange = { viewModel.onLimitChanged(it.toIntOrNull() ?: 0) }, label = { Text("Daily limit (minutes)") })
                    Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(limits) { limit ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(limit.appName, style = MaterialTheme.typography.titleSmall)
                                Text("Limit: ${limit.dailyLimitMinutes} min", style = MaterialTheme.typography.bodySmall)
                                Text(if (limit.blacklisted) "Blacklisted" else "Whitelisted", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { viewModel.delete(limit) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
}
