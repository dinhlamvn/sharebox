package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.extensions.cast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.Executors
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val stateScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

    private val setStateChannel = Channel<T.() -> T>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(T) -> Unit>(capacity = Channel.UNLIMITED)

    private data class Consumer(
        val consumeField: String,
        val liveData: MutableLiveData<Any?>,
        val notifyOnChanged: Boolean = false
    )

    private val consumers = mutableSetOf<Consumer>()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<T> = _state

    init {
        stateScope.launch(Dispatchers.IO) {
            while (isActive) {
                flushQueue()
            }
        }
    }

    protected fun setState(block: T.() -> T) {
        setStateChannel.trySend(block)
    }

    private suspend fun flushQueue() {
        select {
            setStateChannel.onReceive { reducer ->
                val beforeState = state.value
                val newState = reducer.invoke(beforeState)
                if (newState != beforeState) {
                    _state.value = newState
                    consumers.forEach { consumer ->
                        val beforeField =
                            beforeState::class.java.getDeclaredField(consumer.consumeField)
                        beforeField.isAccessible = true
                        val beforeValue = beforeField.get(beforeState)
                        val afterField =
                            newState::class.java.getDeclaredField(consumer.consumeField)
                        afterField.isAccessible = true
                        val afterValue = afterField.get(newState)
                        if (consumer.notifyOnChanged && beforeValue !== afterValue) {
                            consumer.liveData.postValue(afterValue)
                        } else if (!consumer.notifyOnChanged) {
                            consumer.liveData.postValue(afterValue)
                        }
                    }
                }
            }

            withStateChannel.onReceive { block ->
                block.invoke(state.value)
            }
        }
    }

    protected fun withState(block: (T) -> Unit) {
        withStateChannel.trySend(block)
    }

    protected fun execute(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend (T) -> Unit
    ) {
        withState { state ->
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
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
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
