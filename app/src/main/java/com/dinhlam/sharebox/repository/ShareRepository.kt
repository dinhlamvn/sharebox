package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.repository.mapper.ShareToShareDataMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val shareDao: ShareDao,
    private val shareToShareDetailMapper: ShareToShareDataMapper,
    private val userRepository: UserRepository,
) {
    fun insert(data: Share): Boolean = data.runCatching {
        shareDao.insertAll(data)
        true
    }.getOrDefault(false)

    fun findAll() = shareDao.runCatching {
        val shares = find()
        shares.mapNotNull { share ->
            val user = userRepository.findOne(share.shareUserId) ?: return@mapNotNull null
            val shareData = mapShareToShareData(share)
            ShareDetail(share.id, user, share.shareNote, share.createdAt, shareData)
        }
    }.getOrDefault(emptyList())

    fun findAll(shareMode: ShareMode) = shareDao.runCatching {
        val shares = find(shareMode.mode)
        shares.mapNotNull { share ->
            val user = userRepository.findOne(share.shareUserId) ?: return@mapNotNull null
            val shareData = mapShareToShareData(share)
            ShareDetail(share.id, user, share.shareNote, share.createdAt, shareData)
        }
    }.getOrDefault(emptyList())

    private fun mapShareToShareData(share: Share): ShareData = shareToShareDetailMapper.map(share)
}
