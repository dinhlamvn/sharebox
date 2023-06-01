package com.dinhlam.sharebox.data.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.getOrThrow
import com.google.firebase.database.PropertyName

data class RealtimeDBUserObj(
    @get:PropertyName("user_id") val userId: String,
    @get:PropertyName("name") val name: String,
    @get:PropertyName("avatar") val avatar: String,
    @get:PropertyName("level") val level: Int,
    @get:PropertyName("drama") val drama: Int,
    @get:PropertyName("join_date") val joinDate: Long,
) {

    companion object {
        @JvmStatic
        fun from(user: User): RealtimeDBUserObj {
            return RealtimeDBUserObj(
                user.userId, user.name, user.avatar, user.level, user.drama, user.joinDate
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeDBUserObj {
            val userId = jsonMap.getOrThrow("user_id").castNonNull<String>()
            val name = jsonMap.getOrThrow("name").castNonNull<String>()
            val avatar = jsonMap.getOrThrow("avatar").castNonNull<String>()
            val level = jsonMap.getOrThrow("level").castNonNull<Long>().toInt()
            val drama = jsonMap.getOrThrow("drama").castNonNull<Long>().toInt()
            val joinDate = jsonMap.getOrThrow("join_date").castNonNull<Long>()

            return RealtimeDBUserObj(
                userId, name, avatar, level, drama, joinDate
            )
        }
    }
}
