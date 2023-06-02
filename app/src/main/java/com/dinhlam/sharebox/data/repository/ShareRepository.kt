package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.mapper.ShareToShareDetailMapper
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.VideoHelper
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
    private val videoHelper: VideoHelper,
    private val userSharePref: UserSharePref,
) {
    suspend fun insert(
        shareId: String = ShareUtils.createShareId(),
        shareData: ShareData,
        shareNote: String?,
        shareMode: ShareMode,
        shareUserId: String,
        shareDate: Long = nowUTCTimeInMillis()
    ): Share? = shareDao.runCatching {
        val isVideoShare = shareData.cast<ShareData.ShareUrl>()?.runCatching {
            videoHelper.getVideoSource(url)
            true
        }?.getOrDefault(false) ?: false

        val share = Share(
            shareId = shareId,
            shareUserId = shareUserId,
            shareData = shareData,
            shareNote = shareNote,
            shareMode = shareMode,
            shareDate = shareDate,
            isVideoShare = isVideoShare
        )
        insertAll(share)
        share
    }.getOrNull()

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

    suspend fun findAll(): List<ShareDetail> = shareDao.runCatching {
        val shares = find()
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun find(shareUserId: String, limit: Int, offset: Int) = shareDao.runCatching {
        val shares = find(shareUserId, limit, offset)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun find(shareMode: ShareMode) = shareDao.runCatching {
        val shares = find(shareMode)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun find(shareMode: ShareMode, limit: Int, offset: Int) = shareDao.runCatching {
        val shares = find(shareMode, limit, offset)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findRaw(shareMode: ShareMode, limit: Int, offset: Int) = shareDao.runCatching {
        find(shareMode, limit, offset)
    }.getOrDefault(emptyList())

    suspend fun findForVideoMixer(limit: Int, offset: Int) = shareDao.runCatching {
        findForVideoMixer(limit, offset)
    }.getOrDefault(emptyList())

    suspend fun find(shareIds: List<String>) = shareDao.runCatching {
        val shares = find(shareIds)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findShareCommunity(limit: Int, offset: Int) = shareDao.runCatching {
        val shares = findShareCommunity(limit, offset)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    private suspend fun buildShareDetail(share: Share): ShareDetail? {
        val user = userRepository.findOne(share.shareUserId) ?: return null
        val commentCount = commentRepository.count(share.shareId)
        val voteCount = likeRepository.count(share.shareId)
        val bookmarked = bookmarkRepository.findOne(share.shareId) != null
        val liked = likeRepository.find(share.shareId, userSharePref.getActiveUserId()) != null
        return mapper.map(share, user, commentCount, voteCount, bookmarked, liked)
    }
}
