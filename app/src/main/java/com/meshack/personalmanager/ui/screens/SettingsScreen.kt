package com.meshack.personalmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meshack.personalmanager.ui.Screen
import com.meshack.personalmanager.ui.SettingsViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp)
) {
    val userName by viewModel.userName.collectAsState()
    val use24h by viewModel.use24h.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your name", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = userName,
                    onValueChange = { viewModel.onUserNameChanged(it) },
                    label = { Text("Name (shows on dashboard later)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Time format", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = use24h,
                        onClick = { viewModel.onUse24hChanged(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("24-hour") }
                    )
                    SegmentedButton(
                        selected = !use24h,
                        onClick = { viewModel.onUse24hChanged(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("12-hour") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Preview", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        com.meshack.personalmanager.ui.components.formatMinute(13 * 60 + 5, use24h),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        FocusTargetCard(viewModel = viewModel)
        CongratsMessageCard(viewModel = viewModel)
    }
}

@Composable
private fun CongratsMessageCard(viewModel: com.meshack.personalmanager.ui.SettingsViewModel) {
    val saved by viewModel.congratsMessage.collectAsState()
    var text by androidx.compose.runtime.remember(saved) {
        androidx.compose.runtime.mutableStateOf(saved)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Celebration message", style = MaterialTheme.typography.titleMedium)
            Text(
                "Shown with confetti when a goal hits 100%.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value.take(280)
                    viewModel.onCongratsMessageChanged(value)
                },
                label = { Text("Your motivational message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun FocusTargetCard(viewModel: com.meshack.personalmanager.ui.SettingsViewModel) {
    val target by viewModel.focusTargetMinutes.collectAsState()
    var text by androidx.compose.runtime.remember(target) {
        androidx.compose.runtime.mutableStateOf(target.toString())
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Daily focus target", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value.filter { it.isDigit() }.take(3)
                    text.toIntOrNull()?.let { viewModel.onFocusTargetChanged(it) }
                },
                label = { Text("Minutes per day (default 240)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
