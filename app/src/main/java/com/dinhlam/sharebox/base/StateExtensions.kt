package com.dinhlam.sharebox.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <S : State, V> StateManager<S>.observer(
    coroutineScope: CoroutineScope, property: KProperty1<S, V>, block: (V) -> Unit
) {
    state.map {
        Observer1(property.get(it))
    }.distinctUntilChanged().resolveObserver(coroutineScope) { observer ->
        block(observer.value)
    }
}

fun <S : State, V1, V2> StateManager<S>.observer(
    coroutineScope: CoroutineScope,
    property1: KProperty1<S, V1>,
    property2: KProperty1<S, V2>,
    block: (V1, V2) -> Unit
) {
    state.map { state ->
        Observer2(property1.get(state), property2.get(state))
    }.distinctUntilChanged().resolveObserver(coroutineScope) { observer ->
        block(observer.value1, observer.value2)
    }
}

fun <S : State, V1, V2, V3> StateManager<S>.observer(
    coroutineScope: CoroutineScope,
    property1: KProperty1<S, V1>,
    property2: KProperty1<S, V2>,
    property3: KProperty1<S, V3>,
    block: (V1, V2, V3) -> Unit
) {
    state.map { state ->
        Observer3(
            property1.get(state), property2.get(state), property3.get(state)
        )
    }.distinctUntilChanged().resolveObserver(coroutineScope) { observer ->
        block(observer.value1, observer.value2, observer.value3)
    }
}

fun <S : State, V1, V2, V3, V4> StateManager<S>.observer(
    coroutineScope: CoroutineScope,
    property1: KProperty1<S, V1>,
    property2: KProperty1<S, V2>,
    property3: KProperty1<S, V3>,
    property4: KProperty1<S, V4>,
    block: (V1, V2, V3, V4) -> Unit
) {
    state.map { state ->
        Observer4(
            property1.get(state), property2.get(state), property3.get(state), property4.get(state)
        )
    }.distinctUntilChanged().resolveObserver(coroutineScope) { observer ->
        block(observer.value1, observer.value2, observer.value3, observer.value4)
    }
}

fun <S : State, V1, V2, V3, V4, V5> StateManager<S>.observer(
    coroutineScope: CoroutineScope,
    property1: KProperty1<S, V1>,
    property2: KProperty1<S, V2>,
    property3: KProperty1<S, V3>,
    property4: KProperty1<S, V4>,
    property5: KProperty1<S, V5>,
    block: (V1, V2, V3, V4, V5) -> Unit
) {
    state.map { state ->
        Observer5(
            property1.get(state),
            property2.get(state),
            property3.get(state),
            property4.get(state),
            property5.get(state)
        )
    }.distinctUntilChanged().resolveObserver(coroutineScope) { observer ->
        block(
            observer.value1, observer.value2, observer.value3, observer.value4, observer.value5
        )
    }
}

private fun <T> Flow<T>.resolveObserver(coroutineScope: CoroutineScope, block: (T) -> Unit): Job {
    return coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
        yield()
        collectLatest { data ->
            block(data)
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <S : State, reified E : UpdateStateEvent> handlerOf(
    noinline block: (S, E) -> S
): Pair<KClass<UpdateStateEvent>, (S, UpdateStateEvent) -> S> {
    return Pair(
        E::class, block
    ) as Pair<KClass<UpdateStateEvent>, (S, UpdateStateEvent) -> S>
}
