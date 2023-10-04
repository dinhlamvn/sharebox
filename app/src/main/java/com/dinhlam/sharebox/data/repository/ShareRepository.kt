package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.mapper.ShareToShareDetailMapper
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.TrendingShare
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.ShareUtils
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val shareDao: ShareDao,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val likeRepository: LikeRepository,
    private val mapper: ShareToShareDetailMapper,
    private val userSharePref: UserSharePref,
    private val boxRepository: BoxRepository,
) {
    suspend fun insert(
        shareId: String = ShareUtils.createShareId(),
        shareData: ShareData,
        shareNote: String?,
        shareBoxId: String?,
        shareUserId: String,
        shareDate: Long = nowUTCTimeInMillis(),
        synced: Boolean = false,
        isVideoShare: Boolean = false,
    ): Share? = shareDao.runCatching {
        val share = Share(
            shareId = shareId,
            shareUserId = shareUserId,
            shareData = shareData,
            shareNote = shareNote,
            shareBoxId = shareBoxId,
            shareDate = shareDate,
            synced = synced,
            isVideoShare = isVideoShare
        )
        insertAll(share)
        share
    }.getOrNull()

    suspend fun countByUser(userId: String): Int = shareDao.runCatching {
        countByUser(userId)
    }.getOrDefault(0)

    suspend fun update(share: Share): Boolean = shareDao.runCatching {
        update(share)
        true
    }.getOrDefault(false)

    suspend fun findOne(shareId: String) = shareDao.runCatching {
        findOne(shareId)?.let { share ->
            buildShareDetail(share)
        }
    }.getOrNull()

    suspend fun findOneRaw(shareId: String) = shareDao.runCatching {
        findOne(shareId)
    }.getOrNull()

    suspend fun find(shareUserId: String, limit: Int, offset: Int) = shareDao.runCatching {
        val shares = find(shareUserId, limit, offset)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun find(shareIds: List<String>) = shareDao.runCatching {
        val shares = find(shareIds)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findGeneralShares(limit: Int, offset: Int) = shareDao.runCatching {
        val shares = findForGeneral(
            limit = limit,
            offset = offset
        )
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findTrendingShares(limit: Int, offset: Int) = shareDao.runCatching {
        val trendingShares = findForTrending(
            limit = limit,
            offset = offset
        )
        val shares = find(trendingShares.map(TrendingShare::shareId))
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findWhereInBox(shareBoxId: String, limit: Int, offset: Int) = shareDao.runCatching {
        val shares = findWhereInBox(shareBoxId, limit, offset)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findWhereInBox(userId: String, shareBoxId: String, limit: Int, offset: Int) =
        shareDao.runCatching {
            val shares = findWhereInBox(userId, shareBoxId, limit, offset)
            shares.asFlow().mapNotNull(::buildShareDetail).toList()
        }.getOrDefault(emptyList())

    private suspend fun buildShareDetail(share: Share): ShareDetail? = share.runCatching {
        val user = userRepository.findOne(share.shareUserId) ?: return null
        val commentNumber = commentRepository.count(share.shareId)
        val likeNumber = likeRepository.count(share.shareId)
        val bookmarked = bookmarkRepository.bookmarked(share.shareId)
        val liked = likeRepository.liked(share.shareId, userSharePref.getCurrentUserId())
        val topComment = commentRepository.findTopComment(share.shareId)
        val boxDetail = share.shareBoxId?.let { id -> boxRepository.findOne(id) }
        mapper.map(
            share,
            user,
            commentNumber,
            likeNumber,
            bookmarked,
            liked,
            topComment,
            boxDetail,
            share.isVideoShare
        )
    }.getOrNull()
}
