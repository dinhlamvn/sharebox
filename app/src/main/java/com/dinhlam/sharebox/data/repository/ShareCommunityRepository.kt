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

    suspend fun findAll(): List<ShareCommunityDetail> = shareCommunityDao.runCatching {
        find().map { shareCommunity -> mapper.map(shareCommunity) }
    }.getOrDefault(emptyList())
}