package com.meshack.personalmanager.data.repositories

import com.meshack.personalmanager.data.local.daos.NoteDao
import com.meshack.personalmanager.data.local.entities.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(private val dao: NoteDao) {
    fun allNotes(): Flow<List<Note>> = dao.getAllNotes()
    suspend fun insert(note: Note) = dao.insert(note)
    suspend fun delete(note: Note) = dao.delete(note)
}
