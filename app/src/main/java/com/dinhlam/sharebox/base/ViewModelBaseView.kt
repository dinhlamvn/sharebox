package com.dinhlam.sharebox.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.extensions.launchWhenAtLeast
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlin.reflect.KProperty1

interface ViewModelBaseView<S : BaseViewModel.BaseState, VM : BaseViewModel<S>> : LifecycleOwner {
    val viewModel: VM
    fun onStateChanged(state: S)

    fun <R> getState(viewModel: VM, block: (S) -> R) = block.invoke(viewModel.currentState)

    fun <V> onChange(block: (S) -> Unit) = viewModel.stateFlow
        .map { state -> Consumer1(state) }
        .distinctUntilChanged().resolveConsumer(this) { consumer ->
            block(consumer.value)
        }

    fun <V> onChange(property: KProperty1<S, V>, block: (V) -> Unit) = viewModel.stateFlow
        .map { state -> Consumer1(property.get(state)) }
        .distinctUntilChanged().resolveConsumer(this) { consumer ->
            block(consumer.value)
        }

    fun <V1, V2> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2) -> Unit
    ) = viewModel.stateFlow.map { Consumer2(property1.get(it), property2.get(it)) }
        .distinctUntilChanged()
        .resolveConsumer(this) { consumer ->
            block(consumer.value1, consumer.value2)
        }

    fun <V1, V2, V3> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3) -> Unit
    ) = viewModel.stateFlow.map {
        Consumer3(
            property1.get(it),
            property2.get(it),
            property3.get(it)
        )
    }
        .distinctUntilChanged()
        .resolveConsumer(this) { consumer ->
            block(consumer.value1, consumer.value2, consumer.value3)
        }

    fun <V1, V2, V3, V4> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3, V4) -> Unit
    ) = viewModel.stateFlow.map {
        Consumer4(
            property1.get(it),
            property2.get(it),
            property3.get(it),
            property4.get(it)
        )
    }.distinctUntilChanged()
        .resolveConsumer(this) { consumer ->
            block(consumer.value1, consumer.value2, consumer.value3, consumer.value4)
        }

    fun <V1, V2, V3, V4, V5> onChange(
        property1: KProperty1<S, V1>,
        property2: KProperty1<S, V2>,
        property3: KProperty1<S, V3>,
        property4: KProperty1<S, V4>,
        property5: KProperty1<S, V5>,
        lifecycleOwner: LifecycleOwner? = null,
        block: (V1, V2, V3, V4, V5) -> Unit
    ) = viewModel.stateFlow.map {
        Consumer5(
            property1.get(it),
            property2.get(it),
            property3.get(it),
            property4.get(it),
            property5.get(it)
        )
    }.distinctUntilChanged()
        .resolveConsumer(this) { consumer ->
            block(
                consumer.value1,
                consumer.value2,
                consumer.value3,
                consumer.value4,
                consumer.value5
            )
        }

    private fun <T> Flow<T>.resolveConsumer(
        lifecycleOwner: LifecycleOwner, block: (T) -> Unit
    ): Job {
        val flow = flowWhenStarted(lifecycleOwner).distinctUntilChanged()
        val scope = lifecycleOwner.lifecycleScope
        return scope.launch(Dispatchers.Main, start = CoroutineStart.UNDISPATCHED) {
            flow.collectLatest {
                lifecycleOwner.launchWhenAtLeast(Lifecycle.State.STARTED) {
                    block(it)
                }
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