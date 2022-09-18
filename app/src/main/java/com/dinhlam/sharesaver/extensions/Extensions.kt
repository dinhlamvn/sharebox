package com.dinhlam.sharesaver.extensions

inline fun <T, reified R> T?.asThe(): R? {
    return this as? R
}