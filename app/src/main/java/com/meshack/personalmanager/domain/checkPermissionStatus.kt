package com.meshack.personalmanager.domain

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.meshack.personalmanager.util.PermissionStatus
import com.meshack.personalmanager.util.PermissionState
import com.meshack.personalmanager.util.PermissionStatus.*

object PermissionChecker {

    fun checkAll(context: Context): PermissionState {
        val pm = context.packageManager
        val uid = Process.myUid()

        val usage = if (hasUsageAccess(context)) Granted else Denied

        val accessibility = if (isAccessibilityEnabled(context)) Granted else NeedsSetup("Accessibility Service")

        val overlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            Settings.canDrawOverlays(context)) Granted else Denied

        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            pm.checkPermission(android.Manifest.permission.POST_NOTIFICATIONS,
                context.packageName) != android.content.pm.PackageManager.PERMISSION_GRANTED) Denied else Granted

        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            pm.checkPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM,
                context.packageName) != android.content.pm.PackageManager.PERMISSION_GRANTED) Denied else Granted

        val calendar = if (pm.checkPermission(android.Manifest.permission.READ_CALENDAR,
            context.packageName) != android.content.pm.PackageManager.PERMISSION_GRANTED) Denied else Granted

        val battery = if (isIgnoringBatteryOptimizations(context)) Granted else Denied

        return PermissionState(
            usageAccess = usage,
            accessibility = accessibility,
            overlay = overlay,
            notifications = notifications,
            exactAlarm = alarm,
            calendar = calendar,
            batteryOptimization = battery
        )
    }

    private fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val target = context.packageName + "/" + MeshackAccessibilityService::class.java.canonicalName
        return enabledServices.contains(target)
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pwr = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            pwr.isIgnoringBatteryOptimizations(context.packageName)
        } else true
    }

    fun resolveIntent(status: PermissionStatus): Intent? = when (status) {
        is NeedsSetup -> {
            when (status.label) {
                "Accessibility Service" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                else -> null
            }
        }
        is Granted -> null
        else -> when (status) {
            is PermissionStatus.Denied -> when {
                // Usage access
                else -> {
                    try {
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    } catch (t: Throwable) {
                        Intent(Settings.ACTION_SETTINGS)
                    }
                }
            }
            else -> null
        }
    }
}
