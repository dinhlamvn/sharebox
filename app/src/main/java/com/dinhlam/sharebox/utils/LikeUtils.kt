package com.dinhlam.sharebox.utils

import java.util.UUID

object LikeUtils {
    fun createLikeId(): String = UUID.randomUUID().toString()
}