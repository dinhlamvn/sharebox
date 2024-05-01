package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

abstract class BaseViewModel<S : BaseViewModel.BaseState>(initState: S) : ViewModel() {

    interface BaseState

    sealed class AsyncLoad<out T>(val data: T?, val completed: Boolean, val success: Boolean) {
        data object Initialize : AsyncLoad<Nothing>(null, false, false)
        data object Loading : AsyncLoad<Nothing>(null, false, false)
        data class Success<T>(val value: T) : AsyncLoad<T>(value, true, true)
        data class Failed(val error: Throwable) : AsyncLoad<Nothing>(null, true, false)
    }

    private val stateScope =
        CoroutineScope(
            Executors.newSingleThreadExecutor()
                .asCoroutineDispatcher() + CoroutineName("state-scope")
        )

    private data class Consumer<V>(
        val value: V
    )

    private data class Consumer2<V1, V2>(
        val value1: V1,
        val value2: V2
    )

    private val setStateChannel = Channel<suspend S.() -> S>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(S) -> Unit>(Channel.UNLIMITED)

    @Volatile
    var currentState: S = initState
    private val _stateFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = 63,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initState) }
    val stateFlow: Flow<S> = _stateFlow.asSharedFlow()

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

    protected fun setState(block: S.() -> S) {
        setStateChannel.trySend(block)
    }

    protected fun getState(block: (S) -> Unit) {
        getStateChannel.trySend(block)
    }

    protected fun <T> (suspend () -> T).execute(stateReducer: S.(AsyncLoad<T>) -> S): Job {
        setState { stateReducer(AsyncLoad.Loading) }
        return stateScope.launch(Dispatchers.IO) {
            try {
                val result = invoke()
                setState { stateReducer(AsyncLoad.Success(result)) }
            } catch (error: Throwable) {
                setState { stateReducer(AsyncLoad.Failed(error)) }
            }
        }
    }

    protected fun <T> Deferred<T>.execute(stateReducer: S.(AsyncLoad<T>) -> S): Job {
        return suspend { await() }.execute(stateReducer)
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
        lifecycleOwner: LifecycleOwner, property: KProperty1<S, V>, block: (V) -> Unit
    ) {
        stateFlow.map {
            Consumer(property.get(it))
        }.distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value)
            }
    }

    protected fun <V1, V2> consume(
        property1: KProperty1<S, V1>, property2: KProperty1<S, V2>, block: (V1, V2) -> Unit
    ) {
        stateFlow.map { Consumer2(property1.get(it), property2.get(it)) }.distinctUntilChanged()
            .resolveConsumer { consumer ->
                block(consumer.value1, consumer.value2)
            }
    }

    protected fun <V> consume(
        property: KProperty1<S, V>, block: (V) -> Unit
    ) {
        stateFlow.map { Consumer(property.get(it)) }.distinctUntilChanged()
            .resolveConsumer { consumer ->
                block(consumer.value)
            }
    }

    private fun <T> Flow<T>.resolveConsumer(
        lifecycleOwner: LifecycleOwner? = null, block: (T) -> Unit
    ) {
        lifecycleOwner?.let { owner ->
            owner.lifecycleScope.launch(Dispatchers.Main) {
                yield()
                collectLatest { consumerValue ->
                    owner.whenStarted {
                        block(consumerValue)
                    }
                }
            }
        } ?: stateScope.launch(Dispatchers.Main) {
            yield()
            collectLatest(block)
        }
    }
}
