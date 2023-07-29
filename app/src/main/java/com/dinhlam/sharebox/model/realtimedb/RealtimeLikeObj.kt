package com.dinhlam.sharebox.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.getOrThrow
import com.google.firebase.database.PropertyName

data class RealtimeLikeObj(
    @get:PropertyName("like_id") val likeId: String,
    @get:PropertyName("user_id") val userId: String,
    @get:PropertyName("share_id") val shareId: String,
    @get:PropertyName("like_date") val likeDate: Long,
) {

    companion object {
        @JvmStatic
        fun from(like: Like): RealtimeLikeObj {
            return RealtimeLikeObj(
                like.likeId,
                like.userId,
                like.shareId,
                like.likeDate
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeLikeObj {
            val likeId = jsonMap.getOrThrow("like_id").castNonNull<String>()
            val userId = jsonMap.getOrThrow("user_id").castNonNull<String>()
            val shareId = jsonMap.getOrThrow("share_id").castNonNull<String>()
            val likeDate = jsonMap.getOrThrow("like_date").castNonNull<Long>()
            return RealtimeLikeObj(likeId, userId, shareId, likeDate)
        }
    }
}
