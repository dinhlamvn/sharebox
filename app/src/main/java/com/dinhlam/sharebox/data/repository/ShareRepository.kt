package com.dinhlam.sharebox.data.repository

import android.content.Context
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.mapper.ShareToShareDetailMapper
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.utils.ShareUtils
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shareDao: ShareDao,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val mapper: ShareToShareDetailMapper,
    private val userHelper: UserHelper,
    private val boxRepository: BoxRepository,
    private val tagRepository: TagRepository,
) : BaseRepository<String, Share>() {

    override suspend fun insertInternal(entity: Share): Share {
        shareDao.insertAll(entity)
        WorkerUtils.enqueueSyncShareToCloud(context, entity.shareId)
        return entity
    }

    override suspend fun updateInternal(entity: Share, willBeSync: Boolean): Share {
        shareDao.update(entity.copy(synced = !willBeSync))
        if (willBeSync) {
            WorkerUtils.enqueueSyncShareToCloud(context, entity.shareId)
        }
        return entity
    }

    override suspend fun count(): Int {
        return shareDao.count(userHelper.getCurrentUserId())
    }

    override suspend fun delete(entity: Share): Boolean {
        try {
            shareDao.delete(entity)
            return true
        } catch (e: Exception) {
            Logger.error("Delete record $entity from database failed.")
        }
        return false
    }

    override suspend fun readAll(): List<Share> {
        try {
            return shareDao.find(userHelper.getCurrentUserId())
        } catch (e: Exception) {
            Logger.error("Read all record $this from database failed.")
        }
        return emptyList()
    }

    override suspend fun readOne(id: String): Share? {
        try {
            return shareDao.findOne(id)
        } catch (e: Exception) {
            Logger.error("Read one record $this from database failed.")
        }
        return null
    }

    suspend fun insert(
        shareData: ShareData,
        shareNote: String?,
        shareBoxId: String,
        synced: Boolean = false,
        isVideoShare: Boolean = false,
    ): Share? {
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareUserId = userHelper.getCurrentUserId(),
            shareData = shareData,
            shareNote = shareNote,
            shareBoxId = shareBoxId,
            shareDate = nowUTCTimeInMillis(),
            synced = synced,
            isVideoShare = isVideoShare
        )
        return insert(share)
    }

    suspend fun countByUser(userId: String): Int = shareDao.runCatching {
        countByUser(userId)
    }.getOrDefault(0)

    suspend fun findOne(shareId: String) = shareDao.runCatching {
        findOne(shareId)?.let { share ->
            buildShareDetail(share)
        }
    }.getOrNull()

    suspend fun findOneRaw(shareId: String): Share? {
        return try {
            shareDao.findOne(shareId)
        } catch (e: Exception) {
            Logger.error("Query share record: $shareId has error: $e")
            return null
        }
    }

    suspend fun findAll(tagId: Int): List<ShareDetail> {
        return try {
            val shares = shareDao.findAll(userHelper.getCurrentUserId(), tagId)
            shares.asFlow().mapNotNull(::buildShareDetail).toList()
        } catch (e: Exception) {
            Logger.error("Query list share record $tagId has error: $e")
            emptyList()
        }
    }

    suspend fun find(shareIds: List<String>) = shareDao.runCatching {
        val shares = find(shareIds)
        shares.asFlow().mapNotNull(::buildShareDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findRecentlyShares(userId: String, limit: Int, offset: Int): List<ShareDetail> {
        return try {
            val shares = shareDao.findForRecently(userId, limit, offset)
            shares.asFlow().mapNotNull(::buildShareDetail).toList()
        } catch (e: Exception) {
            Logger.error("Query list share record $userId has error: $e")
            emptyList()
        }
    }

    suspend fun findForSyncToCloud(): List<Share> {
        return try {
            shareDao.findForSyncToCloud()
        } catch (e: Exception) {
            Logger.error("Query list share to sync to cloud has error: $e")
            emptyList()
        }
    }

    suspend fun findWhereInBox(shareBoxId: String, limit: Int, offset: Int): List<ShareDetail> {
        return try {
            val shares = shareDao.findWhereInBox(shareBoxId, limit, offset)
            shares.asFlow().mapNotNull(::buildShareDetail).toList()
        } catch (e: Exception) {
            Logger.error("Query list share in box $shareBoxId has error: $e")
            emptyList()
        }
    }

    suspend fun findShareInTrash(limit: Int, offset: Int): List<ShareDetail> {
        return try {
            val shares = shareDao.findShareInTrash(userHelper.getCurrentUserId(), limit, offset)
            shares.asFlow().mapNotNull(::buildShareDetail).toList()
        } catch (e: Exception) {
            Logger.error("Query list share in trash has error: $e")
            emptyList()
        }
    }

    private suspend fun buildShareDetail(share: Share): ShareDetail? = share.runCatching {
        val commentNumber = commentRepository.count(share.shareId)
        val likeNumber = likeRepository.count(share.shareId)
        val liked = likeRepository.liked(share.shareId, userHelper.getCurrentUserId())
        val topComment = commentRepository.findTopComment(share.shareId)
        val boxDetail = if (share.shareBoxId != null) {
            boxRepository.findOne(share.shareBoxId)
        } else {
            null
        }
        val tagColor = if (share.tagId != null) {
            val tag = tagRepository.readOne(share.tagId)
            tag?.tagColor
        } else {
            null
        }
        mapper.map(
            share,
            commentNumber,
            likeNumber,
            liked,
            topComment,
            boxDetail,
            share.isVideoShare,
            share.tagId,
            tagColor
        )
    }.getOrNull()

    suspend fun transferData(anonymousUserId: String, userId: String) {
        shareDao.transferData(anonymousUserId, userId)
    }
}
