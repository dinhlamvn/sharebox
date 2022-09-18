package com.dinhlam.sharesaver.base

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharesaver.extensions.asThe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseData>(initData: T) : ViewModel() {

    interface BaseData

    private val mainHandler = Handler(Looper.getMainLooper())

    private val setDataQueue: Queue<T.() -> T> = ConcurrentLinkedQueue()

    private data class Consumer(
        val consumeField: String, val block: (Any?) -> Unit, val notifyOnChanged: Boolean = false
    )

    private val consumers = mutableSetOf<Consumer>()

    private val _data = OneTimeLiveData<T>()
    val data: LiveData<T> = _data

    init {
        _data.setValue(initData)
    }

    protected fun setData(block: T.() -> T) {
        setDataQueue.offer(block)
        nextSetData()
    }

    private fun setDataInternal(block: T.() -> T) = mainHandler.post {
        val before = data.value
        val newBaseData = data.value?.let(block) ?: return@post
        _data.setValue(newBaseData)
        if (before == null) {
            return@post
        }
        consumers.forEach { consumer ->
            val beforeField = before::class.java.getDeclaredField(consumer.consumeField)
            beforeField.isAccessible = true
            val beforeValue = beforeField.get(before)
            val afterField = newBaseData::class.java.getDeclaredField(consumer.consumeField)
            afterField.isAccessible = true
            val afterValue = afterField.get(newBaseData)
            viewModelScope.launch(Dispatchers.Main) {
                if (consumer.notifyOnChanged && beforeValue != afterValue) {
                    consumer.block.invoke(afterValue)
                } else if (!consumer.notifyOnChanged) {
                    consumer.block.invoke(afterValue)
                }
            }
        }
    }

    @Synchronized
    private fun nextSetData() {
        while (!setDataQueue.isEmpty()) {
            val block = setDataQueue.poll() ?: return
            setDataInternal(block)
        }
    }

    protected fun runWithData(block: (T) -> Unit) {
        nextSetData()
        data.value?.let(block)
    }

    protected fun <R> withData(block: (T) -> R): R {
        nextSetData()
        return data.value!!.let(block)
    }

    protected fun executeWithData(block: suspend (T) -> Unit) {
        nextSetData()
        viewModelScope.launch(Dispatchers.IO) {
            val nonNull = data.value ?: return@launch
            block.invoke(nonNull)
        }
    }

    protected fun execute(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(Dispatchers.IO, block = block)

    fun <T> consume(property: KProperty<T>, block: (T) -> Unit) {
        consumers.add(Consumer(property.name, block.asThe()!!))
    }

    fun <T> consumeOnChange(property: KProperty<T>, block: (T) -> Unit) {
        consumers.add(Consumer(property.name, block.asThe()!!, true))
    }

    fun onClearConsumers() {
        consumers.clear()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}