package com.meshack.personalmanager.util

sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data object Denied : PermissionStatus
    data class NeedsSetup(val label: String) : PermissionStatus
}

data class PermissionState(
    val usageAccess: PermissionStatus = PermissionStatus.Denied,
    val accessibility: PermissionStatus = PermissionStatus.Denied,
    val overlay: PermissionStatus = PermissionStatus.Denied,
    val notifications: PermissionStatus = PermissionStatus.Denied,
    val exactAlarm: PermissionStatus = PermissionStatus.Denied,
    val calendar: PermissionStatus = PermissionStatus.Denied,
    val batteryOptimization: PermissionStatus = PermissionStatus.Denied
)
