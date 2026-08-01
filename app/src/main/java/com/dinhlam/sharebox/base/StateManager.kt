package com.dinhlam.sharebox.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Owns and mutates a screen's immutable state.
 *
 * This class deliberately has no CoroutineScope. The ViewModel owns asynchronous work and calls
 * [update] when that work changes the state. Consequently, clearing a ViewModel also cancels all
 * work associated with its state.
 */
class StateManager<S : Any>(initialState: S) {

    private val mutableState = MutableStateFlow(initialState)

    val state: StateFlow<S> = mutableState.asStateFlow()

    val value: S
        get() = mutableState.value

    /**
     * Atomically creates the next state from the current state.
     *
     * Reducers should be side-effect free: use them only to copy state data.
     */
    fun update(reducer: S.() -> S) {
        mutableState.update(reducer)
    }
}

/**
 * State representation for one asynchronous operation.
 */
sealed interface AsyncResult<out T> {
    data object Idle : AsyncResult<Nothing>
    data object Loading : AsyncResult<Nothing>
    data class Success<T>(val value: T) : AsyncResult<T>
    data class Failure(val error: Throwable) : AsyncResult<Nothing>
}
