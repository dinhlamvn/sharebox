package com.dinhlam.sharebox.utils

import java.util.UUID

object ShareUtils {

    fun createShareId(): String = UUID.randomUUID().toString()
}