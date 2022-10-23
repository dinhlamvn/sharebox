package com.dinhlam.sharesaver.base

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharesaver.extensions.cast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseData>(initData: T) : ViewModel() {

    interface BaseData

    private val mainHandler = Handler(Looper.getMainLooper())

    private val setDataQueue: Queue<T.() -> T> = ConcurrentLinkedQueue()

    private data class Consumer(
        val consumeField: String,
        val anyLiveData: MutableLiveData<Any?>,
        val notifyOnChanged: Boolean = false
    )

    private val consumers = mutableSetOf<Consumer>()

    private val _data = OneTimeLiveData<T>()
    val data: LiveData<T> = _data

    init {
        _data.setValue(initData)
    }

    protected fun setData(block: T.() -> T) {
        setDataQueue.add(block)
        flushSetDataQueue()
    }

    private fun flushSetDataQueue() {
        val block = setDataQueue.poll() ?: return
        setDataInternal(block)
    }

    private fun flushAllSetDataQueue() {
        while (!setDataQueue.isEmpty()) {
            flushSetDataQueue()
        }
    }

    private fun setDataInternal(block: T.() -> T) = viewModelScope.launch(Dispatchers.Main) {
        val before = data.value
        val newBaseData = data.value?.let(block) ?: return@launch
        _data.setValue(newBaseData)
        if (before == null) {
            return@launch
        }
        withContext(Dispatchers.IO) {
            consumers.forEach { consumer ->
                val beforeField = before::class.java.getDeclaredField(consumer.consumeField)
                beforeField.isAccessible = true
                val beforeValue = beforeField.get(before)
                val afterField = newBaseData::class.java.getDeclaredField(consumer.consumeField)
                afterField.isAccessible = true
                val afterValue = afterField.get(newBaseData)
                if (consumer.notifyOnChanged && beforeValue !== afterValue) {
                    consumer.anyLiveData.postValue(afterValue)
                } else if (!consumer.notifyOnChanged) {
                    consumer.anyLiveData.postValue(afterValue)
                }
            }
        }
    }

    protected fun runWithData(block: (T) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            flushAllSetDataQueue()
            data.value?.let(block)
        }
    }

    protected fun <R> withData(block: (T) -> R): R {
        flushAllSetDataQueue()
        return data.value!!.let(block)
    }

    protected fun execute(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend (T) -> Unit,
    ) = viewModelScope.launch(Dispatchers.Main) {
        flushAllSetDataQueue()
        withContext(Dispatchers.IO) {
            try {
                block.invoke(data.value!!)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }

    protected fun executeJob(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    fun <T> consume(lifecycleOwner: LifecycleOwner, property: KProperty<T>, block: (T) -> Unit) {
        val liveData = OneTimeLiveData<T>()
        liveData.observe(lifecycleOwner, block)
        consumers.add(Consumer(property.name, liveData.cast()!!))
    }

    fun <T> consumeOnChange(
        lifecycleOwner: LifecycleOwner,
        property: KProperty<T>,
        block: (T) -> Unit
    ) {
        val liveData = OneTimeLiveData<T>()
        liveData.observe(lifecycleOwner, block)
        consumers.add(Consumer(property.name, liveData.cast()!!, true))
    }

    fun onClearConsumers() {
        consumers.clear()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}