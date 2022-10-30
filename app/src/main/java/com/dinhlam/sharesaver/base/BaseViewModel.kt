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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val mainHandler = Handler(Looper.getMainLooper())

    private val setStateQueue: Queue<T.() -> T> = ConcurrentLinkedQueue()

    private data class Consumer(
        val consumeField: String,
        val anyLiveData: MutableLiveData<Any?>,
        val notifyOnChanged: Boolean = false
    )

    private val consumers = mutableSetOf<Consumer>()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<T> = _state

    protected fun setState(block: T.() -> T) {
        setStateQueue.add(block)
        flushSetStateQueue()
    }

    private fun flushSetStateQueue() {
        val block = setStateQueue.poll() ?: return
        setStateInternal(block)
    }

    private fun flushAllSetStateQueue() {
        while (!setStateQueue.isEmpty()) {
            flushSetStateQueue()
        }
    }

    private fun setStateInternal(block: T.() -> T) = viewModelScope.launch(Dispatchers.Main) {
        val before = state.value
        val newBaseData = state.value.let(block)
        _state.value = newBaseData
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

    protected fun withState(block: (T) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            flushAllSetStateQueue()
            block(state.value)
        }
    }

    protected fun execute(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend (T) -> Unit,
    ) = viewModelScope.launch(Dispatchers.Main) {
        flushAllSetStateQueue()
        withContext(Dispatchers.IO) {
            try {
                block.invoke(state.value)
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

    fun <T> consume(
        lifecycleOwner: LifecycleOwner,
        property: KProperty<T>,
        notifyOnChanged: Boolean = true,
        block: (T) -> Unit
    ) {
        val liveData = OneTimeLiveData<T>(null)
        liveData.observe(lifecycleOwner, block)
        consumers.add(Consumer(property.name, liveData.cast()!!, notifyOnChanged))
    }

    fun onClearConsumers() {
        consumers.clear()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}