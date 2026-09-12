package com.dinhlam.sharebox.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.Executors
import kotlin.collections.get
import kotlin.reflect.KClass

interface State

interface UpdateStateEvent

abstract class UpdateStateHandler<S : State, E : UpdateStateEvent> {
    abstract val eventHandlers: Map<KClass<E>, (S, E) -> S>

    fun handle(state: S, event: E): S {
        val handler = eventHandlers[event::class]
            ?: error("No event handler for this event $event")
        return handler.invoke(state, event)
    }
}

interface StateManager<S : State> {
    val state: Flow<S>

    fun <E : UpdateStateEvent> set(event: E)

    fun get(block: (S) -> Unit)
}

class StateManagerReal<S : State>(
    initState: S,
    private val eventHandler: UpdateStateHandler<S, UpdateStateEvent>
) : StateManager<S> {
    private val stateFlow = MutableStateFlow(initState)

    override val state: Flow<S> = stateFlow

    private val setChannel = Channel<UpdateStateEvent>(Channel.UNLIMITED)

    private val getChannel = Channel<(S) -> Unit>(Channel.UNLIMITED)

    private val coroutineScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    init {
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                select {
                    setChannel.onReceive { event ->
                        stateFlow.update { stateValue ->
                            eventHandler.handle(stateValue, event)
                        }
                    }
                    getChannel.onReceive { block ->
                        block(stateFlow.value)
                    }
                }
            }
        }
    }

    override fun <E : UpdateStateEvent> set(event: E) {
        setChannel.trySend(event)
    }

    override fun get(block: (S) -> Unit) {
        getChannel.trySend(block)
    }
}

