package com.dinhlam.sharebox.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty1

abstract class BaseViewModel<S : BaseViewModel.BaseState>(initState: S) : ViewModel() {

    interface BaseState

    sealed class AsyncLoad<out T>(
        val data: T?,
        val completed: Boolean,
        val success: Boolean
    ) {
        data object UnInitialized : AsyncLoad<Nothing>(null, false, false)
        data object Loading : AsyncLoad<Nothing>(null, false, false)
        data class Success<T>(val value: T) : AsyncLoad<T>(value, true, true)
        data class Failed(val error: Throwable) : AsyncLoad<Nothing>(null, true, false)
    }

    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val setStateChannel = Channel<S.() -> S>(Channel.UNLIMITED)
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
                        val newState = currentState.reducer()
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

    protected fun <T> (suspend () -> T).execute(
        dispatcher: CoroutineDispatcher? = null,
        stateReducer: S.(AsyncLoad<T>) -> S
    ): Job {
        setState { stateReducer(AsyncLoad.Loading) }
        return stateScope.launch(dispatcher ?: EmptyCoroutineContext) {
            try {
                val result = invoke()
                setState { stateReducer(AsyncLoad.Success(result)) }
            } catch (error: Throwable) {
                setState { stateReducer(AsyncLoad.Failed(error)) }
            }
        }
    }

    protected fun <T> Deferred<T>.execute(
        dispatcher: CoroutineDispatcher? = null,
        stateReducer: S.(AsyncLoad<T>) -> S
    ): Job {
        return suspend { await() }.execute(dispatcher, stateReducer)
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

    protected fun <V> onChange(
        property: KProperty1<S, V>, lifecycleOwner: LifecycleOwner? = null, block: (V) -> Unit
    ) {
        stateFlow.map {
            Observer1(property.get(it))
        }.distinctUntilChanged()
            .resolveObserver { observer ->
                block(observer.value)
            }
    }

    protected fun <V1, V2> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        block: (V1, V2) -> Unit
    ) {
        stateFlow.map { state -> Observer2(property1.get(state), property2.get(state)) }
            .distinctUntilChanged()
            .resolveObserver { observer ->
                block(observer.value1, observer.value2)
            }
    }

    protected fun <V1, V2, V3> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        block: (V1, V2, V3) -> Unit
    ) {
        stateFlow.map { state ->
            Observer3(
                property1.get(state),
                property2.get(state),
                property3.get(state)
            )
        }
            .distinctUntilChanged()
            .resolveObserver { observer ->
                block(observer.value1, observer.value2, observer.value3)
            }
    }

    protected fun <V1, V2, V3, V4> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        block: (V1, V2, V3, V4) -> Unit
    ) {
        stateFlow.map { state ->
            Observer4(
                property1.get(state),
                property2.get(state),
                property3.get(state),
                property4.get(state)
            )
        }.distinctUntilChanged()
            .resolveObserver { observer ->
                block(observer.value1, observer.value2, observer.value3, observer.value4)
            }
    }

    protected fun <V1, V2, V3, V4, V5> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        property5: KProperty1<S, V5>,
        block: (V1, V2, V3, V4, V5) -> Unit
    ) {
        stateFlow.map { state ->
            Observer5(
                property1.get(state),
                property2.get(state),
                property3.get(state),
                property4.get(state),
                property5.get(state)
            )
        }.distinctUntilChanged()
            .resolveObserver { observer ->
                block(
                    observer.value1,
                    observer.value2,
                    observer.value3,
                    observer.value4,
                    observer.value5
                )
            }
    }

    private fun <T> Flow<T>.resolveObserver(block: (T) -> Unit): Job {
        return stateScope.launch(start = CoroutineStart.UNDISPATCHED) {
            yield()
            collectLatest { data ->
                block(data)
            }
        }
    }
}
