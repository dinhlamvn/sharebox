package com.dinhlam.sharebox.extensions

fun <T, R> Map<T, R?>.filterValuesNotNull(): Map<T, R> {
    return keys.mapNotNull { key ->
        get(key)?.let { value -> key to value }
    }.toMap()
}

fun <T, R> Map<T, R>.getOrThrow(key: T): R {
    return get(key) ?: throw NoSuchElementException("No element found with key $key")
}

fun <T> List<T>.takeIfNotEmpty(): List<T>? = ifEmpty { null }