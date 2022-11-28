package com.dinhlam.sharesaver.repository

import com.dinhlam.sharesaver.database.dao.ShareDao
import com.dinhlam.sharesaver.database.entity.Share
import javax.inject.Inject

class ShareRepository @Inject constructor(
    private val shareDao: ShareDao
) : Repository<Share, Int> {
    override fun insert(item: Share) {
        shareDao.insertAll(item)
    }

    override fun update(item: Share): Boolean {
        return shareDao.update(item.copy(updatedAt = System.currentTimeMillis())) > 0
    }

    override fun delete(item: Share): Boolean {
        return false
    }

    override fun insertMany(vararg items: Share) {
        shareDao.insertAll(*items)
    }

    override fun updateMany(vararg items: Share) {
    }

    override fun updateById(id: Int, block: (Share) -> Share) {
    }

    override fun get(id: Int): Share {
        return shareDao.getById(id)
    }

    override fun getAll(): List<Share> {
        return shareDao.getAll()
    }

    fun getByFolder(folderId: String): List<Share> {
        return shareDao.getByFolder(folderId)
    }

    fun countByFolder(folderId: String): Int {
        return shareDao.countByFolder(folderId)
    }

    fun deleteByFolder(folderId: String) {
        shareDao.deleteByFolder(folderId)
    }
}
