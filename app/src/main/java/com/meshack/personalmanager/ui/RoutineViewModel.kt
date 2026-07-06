package com.meshack.personalmanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshack.personalmanager.data.repositories.RoutineBlockRepository
import com.meshack.personalmanager.data.local.entities.RoutineBlock
import com.meshack.personalmanager.domain.DayType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineBlockRepository
) : ViewModel() {

    var dayType by mutableStateOf(DayType.WEEKDAY)
        private set

    var showAddDialog by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    val blocks: StateFlow<List<RoutineBlock>> = repository.getBlocksFor(dayType)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentActiveBlock: StateFlow<RoutineBlock?> = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(repository.getCurrentActiveBlock(LocalDate.now()))
            kotlinx.coroutines.delay(60_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onDayTypeChanged(newDayType: DayType) { dayType = newDayType }

    fun openAddDialog() { showAddDialog = true }

    fun dismissAddDialog() { showAddDialog = false }

    fun insert(block: RoutineBlock) {
        viewModelScope.launch {
            val res = repository.insertWithValidation(block)
            res.onSuccess { showAddDialog = false; error = null }
                .onFailure { error = it.message ?: "Failed to save" }
        }
    }

    fun delete(block: RoutineBlock) {
        viewModelScope.launch { repository.delete(block) }
    }
}
