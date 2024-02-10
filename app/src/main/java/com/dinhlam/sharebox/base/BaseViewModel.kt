package com.dinhlam.sharebox.base

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.Executors
import kotlin.reflect.KProperty1

abstract class BaseViewModel<T : BaseViewModel.BaseState>(initState: T) : ViewModel() {

    interface BaseState

    private val stateScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private data class Consumer<V>(
        val value: V
    )

    private val setStateChannel = Channel<suspend T.() -> T>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(T) -> Unit>(Channel.UNLIMITED)

    @Volatile
    var currentState: T = initState
    private val _stateFlow = MutableStateFlow(currentState)
    val stateFlow: Flow<T> = _stateFlow

    private val _toastEvent = OneTimeLiveData(0)
    val toastEvent: LiveData<Int> = _toastEvent
    private var toastJob: Job? = null

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

    protected fun <R : Any?> (suspend () -> R).execute(stateReducer: T.(R) -> T): Job {
        return stateScope.launch(Dispatchers.IO) {
            val result = invoke()
            setState { stateReducer(result) }
        }
    }

    protected fun execute(
        onError: ((Throwable) -> Unit)? = null,
        stateReducer: suspend T.() -> T
    ) = getState { state ->
        suspend { stateReducer(state) }.execute { this }
    }

    protected fun doInBackground(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            onError?.invoke(e)
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

    protected fun postShowToast(@StringRes strRes: Int) {
        if (toastJob?.isCompleted == false) {
            toastJob?.cancel()
        }
        toastJob = viewModelScope.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                _toastEvent.setValue(strRes)
            }
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
