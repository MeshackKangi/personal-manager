package com.meshack.personalmanager

import android.app.Application
import com.meshack.personalmanager.data.database.AppDatabase
import com.meshack.personalmanager.services.AppBlockManager
import com.meshack.personalmanager.util.ForegroundAppHolder
import com.meshack.personalmanager.util.ServiceAliveHolder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MeshackApp : Application() {

    lateinit var appBlockManager: AppBlockManager
        @Inject set

    override fun onCreate() {
        super.onCreate()
        // Trigger DB creation
        AppDatabase.getInstance(this)
    }
}
