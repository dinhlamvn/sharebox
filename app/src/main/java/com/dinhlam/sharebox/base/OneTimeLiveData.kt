package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

open class OneTimeLiveData<T> constructor(init: T?) : MutableLiveData<T>(init) {

    private val pending: AtomicBoolean = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { emittedValue ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(emittedValue)
            }
        }
    }

    override fun setValue(value: T) {
        pending.set(true)
        super.setValue(value)
    }
}
