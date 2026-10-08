package com.meshack.personalmanager.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private var pending: T? = null
    @Volatile private var handled = false

    override fun observeForever(observer: androidx.lifecycle.Observer<in T>) {
        super.observeForever(observer)
        if (!handled) { pending?.let { observer.onChanged(it) }.also { handled = true; pending = null } }
    }

    override fun postValue(value: T) {
        pending = value; handled = false
        super.postValue(value)
    }

    fun get(): T? {
        val v = pending; handled = true; pending = null; return v
    }
}
