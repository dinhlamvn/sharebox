package com.dinhlam.sharekeeper.extensions

inline fun <T, reified R> T?.asThe(): R? {
    return this as? R
}