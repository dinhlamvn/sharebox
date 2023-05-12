package com.dinhlam.sharebox.repository

import android.util.Log
import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.ShareMode
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
            ShareDetail(share.id, user, share.shareNote, share.createdAt, share.shareData)
        }
    }.getOrDefault(emptyList())

    fun findAll(shareMode: ShareMode) = shareDao.runCatching {
        val shares = find(shareMode)
        shares.mapNotNull { share ->
            val user = userRepository.findOne(share.shareUserId) ?: return@mapNotNull null
            ShareDetail(share.id, user, share.shareNote, share.createdAt, share.shareData)
        }
    }.getOrDefault(emptyList())
}
