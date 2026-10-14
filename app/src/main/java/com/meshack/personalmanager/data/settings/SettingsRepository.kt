package com.meshack.personalmanager.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val FOCUS_TARGET_MINUTES = intPreferencesKey("focus_target_minutes")
        val SAMPLE_SEEDED = booleanPreferencesKey("sample_seeded_v2")
        val CONGRATS_MESSAGE = stringPreferencesKey("congrats_message")
    }

    val userName: Flow<String> = context.settingsDataStore.data
        .map { it[Keys.USER_NAME].orEmpty() }

    val use24h: Flow<Boolean> = context.settingsDataStore.data
        .map { it[Keys.USE_24H] ?: true }

    val focusTargetMinutes: Flow<Int> = context.settingsDataStore.data
        .map { it[Keys.FOCUS_TARGET_MINUTES] ?: 240 }

    val sampleSeeded: Flow<Boolean> = context.settingsDataStore.data
        .map { it[Keys.SAMPLE_SEEDED] ?: false }

    val congratsMessage: Flow<String> = context.settingsDataStore.data
        .map {
            it[Keys.CONGRATS_MESSAGE]
                ?: "Goal complete — congratulations! Take a moment to be proud."
        }

    suspend fun setUserName(name: String) {
        context.settingsDataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setUse24h(use24h: Boolean) {
        context.settingsDataStore.edit { it[Keys.USE_24H] = use24h }
    }

    suspend fun setFocusTargetMinutes(minutes: Int) {
        context.settingsDataStore.edit { it[Keys.FOCUS_TARGET_MINUTES] = minutes.coerceIn(15, 960) }
    }

    suspend fun setSampleSeeded() {
        context.settingsDataStore.edit { it[Keys.SAMPLE_SEEDED] = true }
    }

    suspend fun setCongratsMessage(message: String) {
        val trimmed = message.trim().take(280)
        context.settingsDataStore.edit {
            it[Keys.CONGRATS_MESSAGE] =
                trimmed.ifBlank { "Goal complete — congratulations! Take a moment to be proud." }
        }
    }
}
