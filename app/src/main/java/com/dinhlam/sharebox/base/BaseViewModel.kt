package com.dinhlam.sharebox.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.extensions.launchWhenAtLeast
import com.dinhlam.sharebox.model.Consumer1
import com.dinhlam.sharebox.model.Consumer2
import com.dinhlam.sharebox.model.Consumer3
import com.dinhlam.sharebox.model.Consumer4
import com.dinhlam.sharebox.model.Consumer5
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty1

abstract class BaseViewModel<S : BaseViewModel.BaseState>(initState: S) : ViewModel() {

    interface BaseState

    sealed class AsyncLoad<out T>(
        val data: T?,
        val loading: Boolean,
        val completed: Boolean,
        val success: Boolean
    ) {
        data object Initialize : AsyncLoad<Nothing>(null, false, false, false)
        data object Loading : AsyncLoad<Nothing>(null, true, false, false)
        data class Success<T>(val value: T) : AsyncLoad<T>(value, false, true, true)
        data class Failed(val error: Throwable) : AsyncLoad<Nothing>(null, false, true, false)
    }

    private val stateScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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
                val result = this@execute.invoke()
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

    fun <V> onChange(
        property: KProperty1<S, V>, lifecycleOwner: LifecycleOwner? = null, block: (V) -> Unit
    ) {
        stateFlow.map {
            Consumer1(property.get(it))
        }.distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value)
            }
    }

    fun <V1, V2> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2) -> Unit
    ) {
        stateFlow.map { Consumer2(property1.get(it), property2.get(it)) }
            .distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value1, consumer.value2)
            }
    }

    fun <V1, V2, V3> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3) -> Unit
    ) {
        stateFlow.map { Consumer3(property1.get(it), property2.get(it), property3.get(it)) }
            .distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value1, consumer.value2, consumer.value3)
            }
    }

    fun <V1, V2, V3, V4> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3, V4) -> Unit
    ) {
        stateFlow.map {
            Consumer4(
                property1.get(it),
                property2.get(it),
                property3.get(it),
                property4.get(it)
            )
        }.distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(consumer.value1, consumer.value2, consumer.value3, consumer.value4)
            }
    }

    fun <V1, V2, V3, V4, V5> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        property5: KProperty1<S, V5>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3, V4, V5) -> Unit
    ) {
        stateFlow.map {
            Consumer5(
                property1.get(it),
                property2.get(it),
                property3.get(it),
                property4.get(it),
                property5.get(it)
            )
        }
            .distinctUntilChanged()
            .resolveConsumer(lifecycleOwner) { consumer ->
                block(
                    consumer.value1,
                    consumer.value2,
                    consumer.value3,
                    consumer.value4,
                    consumer.value5
                )
            }
    }

    private fun <T> Flow<T>.resolveConsumer(
        lifecycleOwner: LifecycleOwner? = null, block: (T) -> Unit
    ): Job {
        return lifecycleOwner?.let { owner ->
            val flow = flowWhenStarted(owner).distinctUntilChanged()
            val scope = owner.lifecycleScope
            scope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
                flow.collectLatest {
                    owner.launchWhenAtLeast(Lifecycle.State.STARTED) {
                        block(it)
                    }
                }
            }
        } ?: viewModelScope.launch(Dispatchers.Main) {
            yield()
            collectLatest {
                block(it)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T : Any?> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> = flow {
        coroutineScope {
            val startedChannel = startedChannel(owner.lifecycle)
            val flowChannel = produce { collect { send(it) } }

            val nullValue = Any()
            var started: Boolean? = null
            var flowResult: Any? = nullValue
            var isClosed = false

            while (!isClosed) {
                val result = select {
                    onReceive(
                        startedChannel,
                        { flowChannel.cancel(); isClosed = true; nullValue }) { value ->
                        started = value
                        if (flowResult != nullValue && value) {
                            flowResult
                        } else {
                            nullValue
                        }
                    }
                    onReceive(flowChannel, { isClosed = true; nullValue }) { value ->
                        flowResult = value
                        if (started == true) {
                            value
                        } else {
                            nullValue
                        }
                    }
                }
                if (result != nullValue) {
                    @Suppress("UNCHECKED_CAST")
                    emit(result as T)
                }
            }
        }
    }

    private fun startedChannel(owner: Lifecycle): Channel<Boolean> {
        val channel = Channel<Boolean>(Channel.CONFLATED)
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                channel.trySend(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                channel.trySend(false)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                channel.close()
            }
        }
        owner.addObserver(observer)
        channel.invokeOnClose {
            owner.removeObserver(observer)
        }
        return channel
    }

    private inline fun <T : Any?, R : Any?> SelectBuilder<R>.onReceive(
        channel: ReceiveChannel<T>,
        crossinline onClosed: () -> R,
        noinline onReceive: suspend (value: T) -> R
    ) {
        channel.onReceiveCatching { result ->
            if (result.isClosed) {
                onClosed()
            } else {
                onReceive(result.getOrThrow())
            }
        }
    }
}
