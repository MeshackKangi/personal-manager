package com.meshack.personalmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Formats a minute-of-day (0..1439) honoring the 12/24h setting. */
fun formatMinute(minuteOfDay: Int, use24h: Boolean): String {
    val h = (minuteOfDay / 60).coerceIn(0, 23)
    val m = (minuteOfDay % 60).coerceIn(0, 59)
    return if (use24h) {
        "%02d:%02d".format(h, m)
    } else {
        val suffix = if (h < 12) "AM" else "PM"
        val h12 = when (h % 12) {
            0 -> 12
            else -> h % 12
        }
        "%d:%02d %s".format(h12, m, suffix)
    }
}

/** Formats a live clock timestamp honoring the 12/24h setting. */
fun formatClock(now: java.time.LocalDateTime, use24h: Boolean): String {
    val h = now.hour
    val m = now.minute
    val s = now.second
    return if (use24h) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        val suffix = if (h < 12) "AM" else "PM"
        val h12 = when (h % 12) {
            0 -> 12
            else -> h % 12
        }
        "%d:%02d:%02d %s".format(h12, m, s, suffix)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    minuteOfDay: Int,
    use24h: Boolean,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(Icons.Filled.AccessTime, contentDescription = null)
        Text(
            text = "$label ${formatMinute(minuteOfDay, use24h)}",
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    if (showDialog) {
        val state = rememberTimePickerState(
            initialHour = minuteOfDay / 60,
            initialMinute = minuteOfDay % 60,
            is24Hour = use24h
        )
        var showDial by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showDial) TimePicker(state = state) else TimeInput(state = state)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { showDial = !showDial }) {
                            Icon(
                                if (showDial) Icons.Filled.EditCalendar else Icons.Filled.AccessTime,
                                contentDescription = "Toggle picker mode"
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onPick(state.hour * 60 + state.minute)
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
