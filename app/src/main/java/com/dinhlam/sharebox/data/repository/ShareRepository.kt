package com.dinhlam.sharebox.data.repository

import android.util.Log
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.data.model.ShareMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val shareDao: ShareDao,
    private val userRepository: UserRepository,
) {
    fun insert(data: Share): Boolean = data.runCatching {
        shareDao.insertAll(data)
        true
    }.onFailure {
        Log.d("DinhLam", "hehe")
    }.getOrDefault(false)

    fun findAll() = shareDao.runCatching {
        val shares = find()
        shares.mapNotNull { share ->
            val user = userRepository.findOne(share.shareUserId) ?: return@mapNotNull null
            ShareDetail(share.shareId, user, share.shareNote, share.createdAt, share.shareData)
        }
    }.getOrDefault(emptyList())

    fun findAll(shareMode: ShareMode) = shareDao.runCatching {
        val shares = find(shareMode)
        shares.mapNotNull { share ->
            val user = userRepository.findOne(share.shareUserId) ?: return@mapNotNull null
            ShareDetail(share.shareId, user, share.shareNote, share.createdAt, share.shareData)
        }
    }.getOrDefault(emptyList())
}
