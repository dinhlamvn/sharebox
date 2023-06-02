package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.LikeDao
import com.dinhlam.sharebox.data.local.entity.Like
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.utils.LikeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepository @Inject constructor(
    private val likeDao: LikeDao,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) {

    suspend fun likeAndSyncToCloud(
        shareId: String,
        userId: String,
        likeId: String = LikeUtils.createLikeId(),
        likeDate: Long = nowUTCTimeInMillis()
    ): Boolean {
        val like = Like(
            likeId = likeId, shareId = shareId, userId = userId, likeDate = likeDate
        )
        return likeDao.runCatching {
            likeDao.insert(like)
            realtimeDatabaseRepository.push(like)
            true
        }.getOrDefault(false)
    }

    suspend fun like(
        shareId: String,
        userId: String,
        likeId: String = LikeUtils.createLikeId(),
        likeDate: Long = nowUTCTimeInMillis()
    ): Like? {
        val like = Like(likeId = likeId, shareId = shareId, userId = userId, likeDate = likeDate)
        return likeDao.runCatching {
            likeDao.insert(like)
            like
        }.getOrNull()
    }

    suspend fun count(shareId: String) = likeDao.runCatching {
        countByShare(shareId)
    }.getOrDefault(0)

    suspend fun find(shareId: String, userId: String) = likeDao.runCatching {
        find(shareId, userId)
    }.getOrNull()

    suspend fun findOneRaw(likeId: String) = likeDao.runCatching {
        find(likeId)
    }.getOrNull()
}