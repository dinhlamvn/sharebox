package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import java.util.concurrent.Executors
import kotlin.reflect.KProperty1

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val stateScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

    private data class Consumer<V>(
        val value: V
    )

    private val setStateChannel = Channel<suspend T.() -> T>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(T) -> Unit>(Channel.UNLIMITED)

    @Volatile
    var currentState: T = initState
    private val _stateFlow = MutableStateFlow(currentState)
    val stateFlow: Flow<T> = _stateFlow.asSharedFlow()

    init {
        stateScope.launch {
            while (isActive) {
                select {
                    setStateChannel.onReceive { reducer ->
                        val newState = reducer.invoke(currentState)
                        if (newState != currentState) {
                            currentState = newState
                            _stateFlow.emit(newState)
                        }
                    }

                    getStateChannel.onReceive { block ->
                        block(currentState)
                    }
                }
            }
        }
    }

    protected fun setState(block: T.() -> T) {
        setStateChannel.trySend(block)
    }

    protected fun getState(block: (T) -> Unit) {
        getStateChannel.trySend(block)
    }

    protected fun <R : Any?> (suspend () -> R).execute(
        errorCatcher: ((Throwable) -> Unit)? = null, stateReducer: T.(R) -> T
    ): Job {
        return stateScope.launch {
            try {
                val result = invoke()
                setState { stateReducer(result) }
            } catch (e: Throwable) {
                errorCatcher?.invoke(e)
            }
        }
    }

    protected fun <R : Any> Deferred<R>.execute(
        errorCatcher: ((Throwable) -> Unit)? = null, stateReducer: T.(R) -> T
    ): Job {
        return suspend { await() }.execute(errorCatcher, stateReducer)
    }

    protected fun doInBackground(
        errorCatcher: ((Throwable) -> Unit)? = null, block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            errorCatcher?.invoke(e)
        }
    }

    fun <V> consume(
        lifecycleOwner: LifecycleOwner, property: KProperty1<T, V>, block: (V) -> Unit
    ) {
        stateFlow.map { Consumer(property.get(it)) }.distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value)
            }
    }

    protected fun <V> consume(
        property: KProperty1<T, V>, block: suspend (V) -> Unit
    ) {
        stateFlow.map { Consumer(property.get(it)) }.distinctUntilChanged()
            .resolveConsumer { consumer ->
                block(consumer.value)
            }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        stateScope.cancel()
    }

    private fun <T> Flow<T>.resolveConsumer(
        lifecycleOwner: LifecycleOwner? = null, block: suspend (T) -> Unit
    ) {
        lifecycleOwner?.let { owner ->
            owner.lifecycleScope.launch {
                yield()
                collectLatest {
                    owner.whenStarted { block(it) }
                }
            }
        } ?: stateScope.launch {
            yield()
            collectLatest(block)
        }
    }
}
