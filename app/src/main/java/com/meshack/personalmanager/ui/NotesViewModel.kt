package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.NoteRepository
import com.meshack.personalmanager.data.local.entities.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    var title by mutableStateOf("")
        private set

    var content by mutableStateOf("")
        private set

    val notes: StateFlow<List<Note>> = noteRepository.allNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTitleChanged(value: String) { title = value }
    fun onContentChanged(value: String) { content = value }

    fun save() {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                noteRepository.insert(Note(title = title.trim(), content = content.trim()))
                title = ""
                content = ""
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch { noteRepository.delete(note) }
    }
}
