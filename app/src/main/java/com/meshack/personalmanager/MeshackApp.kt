package com.meshack.personalmanager

import android.app.Application
import com.meshack.personalmanager.data.database.AppDatabase
import com.meshack.personalmanager.data.samples.SampleDataSeeder
import com.meshack.personalmanager.services.AppBlockManager
import com.meshack.personalmanager.util.ForegroundAppHolder
import com.meshack.personalmanager.util.ServiceAliveHolder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MeshackApp : Application() {

    lateinit var appBlockManager: AppBlockManager
        @Inject set

    @Inject lateinit var sampleDataSeeder: SampleDataSeeder
    @Inject lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Trigger DB creation
        AppDatabase.getInstance(this)
        // One-time sample content for a full first-run visualization.
        applicationScope.launch {
            runCatching { sampleDataSeeder.seedIfEmpty() }
                .onFailure { android.util.Log.e("SampleSeeder", "seed failed", it) }
        }
    }
}
