package com.dinhlam.sharebox.extensions

fun <T, R> Map<T, R?>.filterValuesNotNull(): Map<T, R> {
    return keys.mapNotNull { key ->
        get(key)?.let { value -> key to value }
    }.toMap()
}