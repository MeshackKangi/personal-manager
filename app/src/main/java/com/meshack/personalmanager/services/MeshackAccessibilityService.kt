package com.meshack.personalmanager.services

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.meshack.personalmanager.util.ForegroundAppHolder
import com.meshack.personalmanager.util.ServiceAliveHolder

class MeshackAccessibilityService : android.accessibilityservice.AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceAliveHolder.setAlive(true)
        Log.d("MeshackA11y", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == applicationContext.packageName) return
        ForegroundAppHolder.foregroundPackage = pkg
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        ServiceAliveHolder.setAlive(false)
        super.onDestroy()
    }
}
