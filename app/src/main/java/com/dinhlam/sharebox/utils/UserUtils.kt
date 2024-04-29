package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.extensions.coerceMinMax
import java.util.UUID

object UserUtils {

    const val ANONYMOUS_AVATAR_URL = "https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/default-avatar.png"

    fun createUserId(uniqueId: String) =
        UUID.nameUUIDFromBytes(uniqueId.toByteArray()).toString()

    fun getLevelTitle(level: Int) = arrayOf(
        "Newbie",
        "Junior Member",
        "Senior Member",
        "Specialist Member"
    )[level.coerceMinMax(0, 3)]
}