package com.meshack.personalmanager.util

object ServiceAliveHolder {
    private val _alive = SingleLiveEvent<Boolean>()
    val alive: SingleLiveEvent<Boolean> get() = _alive
    var isAlive: Boolean = false
        private set

    fun setAlive(value: Boolean) {
        isAlive = value
        _alive.postValue(value)
    }
}
