package com.dinhlam.sharebox.base

data class Observer1<V>(
    val value: V
)

data class Observer2<V1, V2>(
    val value1: V1,
    val value2: V2
)

data class Observer3<V1, V2, V3>(
    val value1: V1,
    val value2: V2,
    val value3: V3,
)

data class Observer4<V1, V2, V3, V4>(
    val value1: V1,
    val value2: V2,
    val value3: V3,
    val value4: V4,
)

data class Observer5<V1, V2, V3, V4, V5>(
    val value1: V1,
    val value2: V2,
    val value3: V3,
    val value4: V4,
    val value5: V5,
)