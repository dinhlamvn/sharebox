package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.CommentDao
import com.dinhlam.sharebox.data.local.entity.Comment
import com.dinhlam.sharebox.data.mapper.CommentToCommentDetailMapper
import com.dinhlam.sharebox.data.model.CommentDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val commentDao: CommentDao,
    private val commentToCommentDetailMapper: CommentToCommentDetailMapper,
    private val userRepository: UserRepository,
) {

    fun insert(shareId: String, userId: String, content: String?): Boolean =
        commentDao.runCatching {
            insert(Comment(shareId = shareId, shareUserId = userId, content = content))
            true
        }.getOrDefault(false)

    fun find(shareId: String): List<CommentDetail> {
        return commentDao.runCatching {
            val comments = find(shareId)
            comments.mapNotNull { comment ->
                val userDetail =
                    userRepository.findOne(comment.shareUserId) ?: return@mapNotNull null
                commentToCommentDetailMapper.map(comment, userDetail)
            }
        }.getOrDefault(emptyList())
    }

    fun count(shareId: String): Int = commentDao.runCatching {
        commentDao.count(shareId)
    }.getOrDefault(0)
}