package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.ShareCommunityDao
import com.dinhlam.sharebox.data.local.entity.ShareCommunity
import com.dinhlam.sharebox.data.mapper.ShareCommunityToShareCommunityDetailMapper
import com.dinhlam.sharebox.data.model.ShareCommunityDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareCommunityRepository @Inject constructor(
    private val shareCommunityDao: ShareCommunityDao,
    private val mapper: ShareCommunityToShareCommunityDetailMapper
) {

    suspend fun insert(shareId: String, sharePower: Int = 0): Boolean =
        shareCommunityDao.runCatching {
            val shareCommunity = ShareCommunity(shareId = shareId, sharePower = sharePower)
            insert(shareCommunity)
            true
        }.getOrDefault(false)

    suspend fun insert(id: Int, shareId: String, sharePower: Int = 0): Boolean =
        shareCommunityDao.runCatching {
            val shareCommunity = ShareCommunity(id, shareId = shareId, sharePower = sharePower)
            upsert(shareCommunity)
            true
        }.getOrDefault(false)

    suspend fun findAll(): List<ShareCommunityDetail> = shareCommunityDao.runCatching {
        find().map { shareCommunity -> mapper.map(shareCommunity) }
    }.getOrDefault(emptyList())

    suspend fun findOne(shareId: String): ShareCommunityDetail? = shareCommunityDao.runCatching {
        find(shareId)?.let { shareCommunity -> mapper.map(shareCommunity) }
    }.getOrDefault(null)

    suspend fun findShareToCleanUp(timeToClean: Long): List<ShareCommunity> = shareCommunityDao.runCatching {
        findShareToCleanUp(timeToClean)
    }.getOrDefault(emptyList())

    suspend fun delete(shareCommunity: ShareCommunity) = shareCommunityDao.runCatching {
        delete(shareCommunity)
        true
    }.getOrDefault(false)
}