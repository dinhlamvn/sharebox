package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.extensions.cast
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select
import java.util.concurrent.Executors
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val stateScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

    private data class Consumer(
        val consumeField: String,
        val liveData: MutableLiveData<Any?>,
        val notifyOnChanged: Boolean = false
    )

    private val setStateChannel = Channel<T.() -> T>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(T) -> Unit>(Channel.UNLIMITED)

    private val consumers = mutableSetOf<Consumer>()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<T> = _state

    init {
        stateScope.launch {
            while (isActive) {
                val currentState = state.value
                select {
                    setStateChannel.onReceive { reducer ->
                        val newState = reducer.invoke(currentState)
                        if (newState != currentState) {
                            _state.emit(newState)
                        }
                        notifyConsumer(currentState, newState)
                    }

                    getStateChannel.onReceive { block ->
                        block(currentState)
                    }
                }
            }
        }
    }

    private fun notifyConsumer(oldState: T, newState: T) {
        val consumerIterator = consumers.iterator()
        while (consumerIterator.hasNext()) {
            val consumer = consumerIterator.next()
            val beforeField = oldState::class.java.getDeclaredField(consumer.consumeField)
            beforeField.isAccessible = true
            val beforeValue = beforeField.get(oldState)
            val afterField = newState::class.java.getDeclaredField(consumer.consumeField)
            afterField.isAccessible = true
            val afterValue = afterField.get(newState)
            if (consumer.notifyOnChanged && beforeValue !== afterValue) {
                consumer.liveData.postValue(afterValue)
            } else if (!consumer.notifyOnChanged) {
                consumer.liveData.postValue(afterValue)
            }
        }
    }

    protected fun setState(block: T.() -> T) {
        setStateChannel.trySend(block)
    }

    protected fun getState(block: (T) -> Unit) {
        getStateChannel.trySend(block)
    }

    protected fun execute(
        onError: ((Throwable) -> Unit)? = null, block: suspend (T) -> Unit
    ) {
        getState { state ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    block.invoke(state)
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }

    protected fun executeJob(
        onError: ((Throwable) -> Unit)? = null, block: suspend CoroutineScope.() -> Unit
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

    override fun onCleared() {
        super.onCleared()
        val consumerIterator = consumers.iterator()
        while (consumerIterator.hasNext()) {
            consumerIterator.next()
            consumerIterator.remove()
        }
        viewModelScope.cancel()
    }
}
