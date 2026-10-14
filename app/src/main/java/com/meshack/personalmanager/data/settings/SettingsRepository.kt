package com.meshack.personalmanager.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USE_24H = booleanPreferencesKey("use_24h")
    }

    val userName: Flow<String> = context.settingsDataStore.data
        .map { it[Keys.USER_NAME].orEmpty() }

    val use24h: Flow<Boolean> = context.settingsDataStore.data
        .map { it[Keys.USE_24H] ?: true }

    suspend fun setUserName(name: String) {
        context.settingsDataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setUse24h(use24h: Boolean) {
        context.settingsDataStore.edit { it[Keys.USE_24H] = use24h }
    }
}
