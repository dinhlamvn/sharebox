package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.mapper.CommentToCommentDetailMapper
import com.dinhlam.sharebox.data.model.CommentDetail
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao,
    private val commentToCommentDetailMapper: CommentToCommentDetailMapper,
    private val userRepository: UserRepository,
) {
    suspend fun insert(
        commentId: String,
        shareId: String,
        userId: String,
        content: String?,
        commentDate: Long = nowUTCTimeInMillis()
    ): Comment? = commentDao.runCatching {
        val comment = Comment(
            commentId = commentId,
            shareId = shareId,
            shareUserId = userId,
            content = content,
            commentDate = commentDate
        )
        insert(comment)
        comment
    }.getOrNull()

    suspend fun find(shareId: String): List<CommentDetail> {
        return commentDao.runCatching {
            val comments = find(shareId)
            comments.mapNotNull { comment ->
                val userDetail =
                    userRepository.findOne(comment.shareUserId) ?: return@mapNotNull null
                commentToCommentDetailMapper.map(comment, userDetail)
            }
        }.getOrDefault(emptyList())
    }

    suspend fun findOneRaw(commentId: String): Comment? {
        return commentDao.runCatching {
            findOne(commentId)
        }.getOrNull()
    }

    suspend fun count(shareId: String): Int = commentDao.runCatching {
        commentDao.count(shareId)
    }.getOrDefault(0)
}