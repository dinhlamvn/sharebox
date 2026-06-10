package com.dinhlam.sharebox.base

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.reflect.KClass

interface BaseState

interface UpdateStateEvent<S : BaseState>

abstract class StateManager<S : BaseState> {
    abstract val state: StateFlow<S>
    abstract fun set(event: UpdateStateEvent<S>)
}

interface UpdateStateHandler<S : BaseState, E : UpdateStateEvent<S>> {
    fun handle(state: S, event: UpdateStateEvent<S>): S
}

class StateManagerReal<S : BaseState>(
    initState: S,
    val handlers: Map<KClass<UpdateStateEvent<S>>, UpdateStateHandler<S, UpdateStateEvent<S>>>
) : StateManager<S>() {

    private val _state = MutableStateFlow(initState)
    override val state: StateFlow<S>
        get() = _state.asStateFlow()

    private val setStateChannel = Channel<UpdateStateEvent<S>>(Channel.UNLIMITED)

    private val stateScope =
        CoroutineScope(CoroutineName("state-manager-scope") + Dispatchers.IO + SupervisorJob())

    init {
        stateScope.launch {
            while (isActive) {
                select {
                    setStateChannel.onReceive { event ->
                        _state.update { currentState ->
                            val handler = handlers[event::class]
                                ?: error("No handler for this event --> $event")
                            handler.handle(currentState, event)
                        }
                    }
                }
            }
        }
    }

    override fun set(event: UpdateStateEvent<S>) {
        setStateChannel.trySend(event)
    }
}