package com.dinhlam.keepmyshare.extensions

inline fun <T, reified R> T?.cast(): R? {
    return this as? R
}