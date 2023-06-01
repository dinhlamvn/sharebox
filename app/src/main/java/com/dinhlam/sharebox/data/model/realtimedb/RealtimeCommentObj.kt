package com.dinhlam.sharebox.data.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.getOrThrow
import com.google.firebase.database.PropertyName

data class RealtimeCommentObj(
    @get:PropertyName("comment_id") val commentId: String,
    @get:PropertyName("user_id") val userId: String,
    @get:PropertyName("share_id") val shareId: String,
    @get:PropertyName("content") val content: String?,
    @get:PropertyName("comment_date") val commentDate: Long,
) {

    companion object {
        @JvmStatic
        fun from(comment: Comment): RealtimeCommentObj {
            return RealtimeCommentObj(
                comment.commentId,
                comment.shareUserId,
                comment.shareId,
                comment.content,
                comment.commentDate
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeCommentObj {
            val commentId = jsonMap.getOrThrow("comment_id").castNonNull<String>()
            val userId = jsonMap.getOrThrow("user_id").castNonNull<String>()
            val shareId = jsonMap.getOrThrow("share_id").castNonNull<String>()
            val content = jsonMap["content"]?.toString()
            val commentDate = jsonMap.getOrThrow("comment_date").castNonNull<Long>()
            return RealtimeCommentObj(commentId, userId, shareId, content, commentDate)
        }
    }
}
