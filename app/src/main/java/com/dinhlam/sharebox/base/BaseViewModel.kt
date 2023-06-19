package com.dinhlam.sharebox.base

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.extensions.castNonNull
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
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val stateScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

    private data class Consumer(
        val consumeField: String,
        val liveData: MutableLiveData<Any?>,
        val notifyOnChanged: Boolean = false
    )

    private data class ConsumerInternal(
        val consumeField: String,
        val changeNotifier: suspend (Any?) -> Unit,
        val notifyOnChanged: Boolean = false
    )

    private val setStateChannel = Channel<suspend T.() -> T>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(T) -> Unit>(Channel.UNLIMITED)

    private val consumers = CopyOnWriteArraySet<Consumer>()

    private val internalConsumers = CopyOnWriteArraySet<ConsumerInternal>()

    private val _state = MutableStateFlow(initState)
    val state: StateFlow<T> = _state

    private val _toastEvent = OneTimeLiveData(0)
    val toastEvent: LiveData<Int> = _toastEvent

    init {
        stateScope.launch {
            while (isActive) {
                val currentState = state.value
                select {
                    setStateChannel.onReceive { reducer ->
                        val newState = reducer.invoke(currentState)
                        if (newState != currentState) {
                            _state.emit(newState)
                            notifyConsumers(currentState, newState)
                        }
                    }

                    getStateChannel.onReceive { block ->
                        block(currentState)
                    }
                }
            }
        }
    }

    private suspend fun notifyConsumers(oldState: T, newState: T) {
        val consumerIterator = consumers.iterator()
        while (consumerIterator.hasNext() && stateScope.isActive && viewModelScope.isActive) {
            val consumer = consumerIterator.next()
            val beforeValue = getFieldStateValue(oldState, consumer.consumeField)
            val afterValue = getFieldStateValue(newState, consumer.consumeField)
            if (consumer.notifyOnChanged && beforeValue != afterValue) {
                consumer.liveData.postValue(afterValue)
            } else if (!consumer.notifyOnChanged) {
                consumer.liveData.postValue(afterValue)
            }
        }

        val internalConsumerIterator = internalConsumers.iterator()
        while (internalConsumerIterator.hasNext() && stateScope.isActive && viewModelScope.isActive) {
            val consumer = internalConsumerIterator.next()
            val beforeValue = getFieldStateValue(oldState, consumer.consumeField)
            val afterValue = getFieldStateValue(newState, consumer.consumeField)
            if (consumer.notifyOnChanged && beforeValue != afterValue) {
                consumer.changeNotifier.invoke(afterValue)
            } else if (!consumer.notifyOnChanged) {
                consumer.changeNotifier.invoke(afterValue)
            }
        }
    }

    private fun getFieldStateValue(state: T, fieldName: String): Any? {
        val field = state::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(state)
    }

    protected fun setState(block: suspend T.() -> T) {
        setStateChannel.trySend(block)
    }

    protected fun getState(block: (T) -> Unit) {
        getStateChannel.trySend(block)
    }

    protected fun execute(
        context: CoroutineContext = Dispatchers.IO,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend T.() -> T
    ) {
        viewModelScope.launch(context) {
            try {
                setState { block(this) }
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }

    protected fun backgroundTask(
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
        val consumer = Consumer(property.name, liveData.castNonNull(), notifyOnChanged)
        consumers.add(consumer)
        state.value.runCatching {
            consumer.liveData.postValue(getFieldStateValue(this, property.name))
        }
    }

    protected fun <T> consume(
        property: KProperty<T>, notifyOnChanged: Boolean = true, block: suspend (T) -> Unit
    ) {
        val consumerInternal = ConsumerInternal(property.name, block.castNonNull(), notifyOnChanged)
        internalConsumers.add(consumerInternal)
        state.value.runCatching {
            viewModelScope.launch {
                consumerInternal.changeNotifier.invoke(
                    getFieldStateValue(
                        this@runCatching,
                        property.name
                    )
                )
            }
        }
    }

    protected fun postShowToast(@StringRes strRes: Int) = _toastEvent.postValue(strRes)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        stateScope.cancel()
        consumers.clear()
        internalConsumers.clear()
    }
}
