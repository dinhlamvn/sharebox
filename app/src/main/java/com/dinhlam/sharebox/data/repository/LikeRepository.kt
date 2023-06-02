package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.entity.Like
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepository @Inject constructor(
    private val likeDao: LikeDao
) {

    suspend fun like(shareId: String, userId: String): Boolean {
        val like = Like(shareId = shareId, userId = userId)
        return likeDao.runCatching {
            likeDao.insert(like)
            true
        }.getOrDefault(false)
    }

    suspend fun count(shareId: String) = likeDao.runCatching {
        countVote(shareId)
    }.getOrDefault(0)

    suspend fun find(shareId: String, userId: String) = likeDao.runCatching {
        find(shareId, userId)
    }.getOrNull()
}