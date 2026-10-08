package com.meshack.personalmanager.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.meshack.personalmanager.domain.PermissionChecker
import com.meshack.personalmanager.data.repositories.AppLimitRepository
import com.meshack.personalmanager.data.repositories.RoutineBlockRepository
import com.meshack.personalmanager.util.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val application: Application,
    private val routineBlockRepository: RoutineBlockRepository,
    private val appLimitRepository: AppLimitRepository
) : ViewModel() {

    var state by mutableStateOf(PermissionState())
        private set

    init { refresh() }

    fun refresh() {
        state = PermissionChecker.checkAll(application)
    }

    fun actionFor(status: com.meshack.personalmanager.util.PermissionStatus): android.content.Intent? = PermissionChecker.resolveIntent(status)
}
