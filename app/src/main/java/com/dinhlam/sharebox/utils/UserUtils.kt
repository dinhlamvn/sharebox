package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.extensions.coerceMinMax
import java.util.UUID

object UserUtils {

    val fakeUserId: String by lazy { createUserId("dinhlamvn353@gmail.com") }

    fun createUserId(email: String) =
        UUID.nameUUIDFromBytes("dinhlamvn353".toByteArray()).toString()

    fun getLevelTitle(level: Int) = arrayOf(
        "Newbie",
        "Junior Member",
        "Senior Member",
        "Specialist Member"
    )[level.coerceMinMax(0, 3)]
}