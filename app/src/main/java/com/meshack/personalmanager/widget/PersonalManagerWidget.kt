package com.meshack.personalmanager.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.meshack.personalmanager.R.string as R

class PersonalManagerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // materialized later via DataStore/Room; placeholder keeps provider valid
        appWidgetIds.forEach { appWidgetManager.notifyAppWidgetViewDataChanged(it, android.R.id.list) }
    }
}
