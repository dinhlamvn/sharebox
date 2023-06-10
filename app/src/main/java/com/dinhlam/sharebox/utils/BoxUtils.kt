package com.dinhlam.sharebox.utils

import java.util.UUID

object BoxUtils {

    fun createBoxId(name: String): String {
        val lowerCase = name.lowercase().replace(" ", "_")
        return UUID.nameUUIDFromBytes(lowerCase.toByteArray()).toString()
    }
}