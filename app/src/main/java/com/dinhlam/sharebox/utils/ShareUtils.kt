package com.dinhlam.sharebox.utils

import java.util.UUID

object ShareUtils {

    const val SHARE_MODE_COMMUNITY = "community"
    const val SHARE_MODE_PERSONAL = "personal"

    fun createShareId(): String = UUID.randomUUID().toString()
}