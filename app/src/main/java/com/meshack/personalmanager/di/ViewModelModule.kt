package com.meshack.personalmanager.di

import com.meshack.personalmanager.ui.AppLimitsViewModel
import com.meshack.personalmanager.ui.HomeViewModel
import com.meshack.personalmanager.ui.PermissionsViewModel
import com.meshack.personalmanager.ui.PomodoroViewModel
import com.meshack.personalmanager.ui.RoutineViewModel
import com.meshack.personalmanager.ui.TasksViewModel
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelModule {
    // Hilt auto-generates assisted keys; just declare abstract functions in di package for assisted injection
}
