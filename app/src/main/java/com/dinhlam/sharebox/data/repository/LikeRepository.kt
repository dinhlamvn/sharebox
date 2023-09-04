package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.utils.LikeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepository @Inject constructor(
    private val likeDao: LikeDao
) {

    suspend fun like(
        shareId: String,
        userId: String,
        likeId: String = LikeUtils.createLikeId(),
        likeDate: Long = nowUTCTimeInMillis(),
        synced: Boolean = false,
    ): Like? {
        val like = Like(
            likeId = likeId,
            shareId = shareId,
            userId = userId,
            likeDate = likeDate,
            synced = synced
        )
        return likeDao.runCatching {
            likeDao.insert(like)
            like
        }.getOrNull()
    }

    suspend fun update(like: Like): Boolean = likeDao.runCatching {
        update(like)
        true
    }.getOrDefault(false)

    suspend fun count(shareId: String) = likeDao.runCatching {
        countByShare(shareId)
    }.getOrDefault(0)

    suspend fun find(shareId: String, userId: String) = likeDao.runCatching {
        find(shareId, userId)
    }.getOrNull()

    suspend fun liked(shareId: String, userId: String) = likeDao.runCatching {
        find(shareId, userId)?.let { true } ?: false
    }.getOrDefault(false)

    suspend fun findOneRaw(likeId: String) = likeDao.runCatching {
        find(likeId)
    }.getOrNull()

    suspend fun countByUserShare(userId: String): Int = likeDao.runCatching {
        countByUserShare(userId)
    }.getOrDefault(0)
}