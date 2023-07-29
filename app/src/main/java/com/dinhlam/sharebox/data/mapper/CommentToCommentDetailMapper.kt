package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.model.CommentDetail
import com.dinhlam.sharebox.model.UserDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentToCommentDetailMapper @Inject constructor() {
    fun map(comment: Comment, userDetail: UserDetail): CommentDetail {
        return CommentDetail(
            comment.id,
            comment.shareId,
            comment.content,
            comment.commentDate,
            comment.createdAt,
            userDetail
        )
    }
}