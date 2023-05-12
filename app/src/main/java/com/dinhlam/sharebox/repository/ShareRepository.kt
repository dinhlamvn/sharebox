package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.ShareDao
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.FolderShareCount
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.repository.mapper.ShareToShareDetailMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val shareDao: ShareDao,
    private val shareToShareDetailMapper: ShareToShareDetailMapper,
) : Repository<Share, Int> {
    override fun insert(data: Share) {
        shareDao.insertAll(data)
    }

    override fun update(data: Share): Boolean {
        return shareDao.update(data.copy(updatedAt = System.currentTimeMillis())) > 0
    }

    override fun delete(data: Share): Boolean {
        return shareDao.delete(data) > 0
    }

    override fun insertMany(vararg items: Share) {
        shareDao.insertAll(*items)
    }

    override fun updateMany(vararg items: Share) {
    }

    override fun updateById(id: Int, block: (Share) -> Share) {
    }

    override fun find(id: Int): Share? {
        return shareDao.getById(id)
    }

    override fun findAll(sortType: SortType): List<Share> {
        return shareDao.getAll()
    }

    fun getByFolder(folderId: String): List<Share> {
        return shareDao.getByFolder()
    }

    fun countByFolder(folderId: String): Int {
        return shareDao.countByFolder()
    }

    fun deleteByFolder(folderId: String) {
        shareDao.deleteByFolder()
    }

    fun getRecentList(): List<Share> {
        return shareDao.getRecentList()
    }

    override fun search(query: String): List<Share> {
        return shareDao.search(query)
    }

    fun getFolderShareCount(): List<FolderShareCount> {
        return emptyList()
    }

    override fun rowCount(): Int {
        return shareDao.count()
    }

    fun findAll() = shareDao.runCatching {
        find().map(::mapShareToShareDetail)
    }.getOrDefault(emptyList())

    fun findAll(shareMode: ShareMode) = shareDao.runCatching {
        find(shareMode.mode).map(::mapShareToShareDetail)
    }.getOrDefault(emptyList())

    private fun mapShareToShareDetail(share: Share): ShareDetail =
        shareToShareDetailMapper.map(share)
}
