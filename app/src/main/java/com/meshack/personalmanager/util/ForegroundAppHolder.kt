package com.meshack.personalmanager.util

import androidx.lifecycle.LiveData
object ForegroundAppHolder {
    // Live value of the current foreground package
    private val _current = SingleLiveEvent<String>()
    val current: LiveData<String> get() = _current
    var foregroundPackage: String = ""
        set(value) { field = value; _current.postValue(value) }
}
