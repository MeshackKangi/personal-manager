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
import com.meshack.personalmanager.ui.NotesViewModel
import com.meshack.personalmanager.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onNavigate: (Screen) -> Unit, viewModel: NotesViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Notes") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Note", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.onTitleChanged(it) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = viewModel.content, onValueChange = { viewModel.onContentChanged(it) }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(note.title, style = MaterialTheme.typography.titleSmall)
                            Text(note.content, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { viewModel.delete(note) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
