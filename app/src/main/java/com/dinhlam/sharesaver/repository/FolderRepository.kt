package com.dinhlam.sharesaver.repository

import com.dinhlam.sharesaver.database.dao.FolderDao
import com.dinhlam.sharesaver.database.entity.Folder
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val folderDao: FolderDao
) : Repository<Folder, String> {
    override fun insert(item: Folder) {
        folderDao.insertAll(item)
    }

    override fun update(item: Folder): Boolean {
        return folderDao.update(item.copy(updatedAt = System.currentTimeMillis())) > 0
    }

    override fun delete(item: Folder): Boolean {
        return folderDao.delete(item) > 0
    }

    override fun insertMany(vararg items: Folder) {
        folderDao.insertAll(*items)
    }

    override fun updateMany(vararg items: Folder) {
    }

    override fun updateById(id: String, block: (Folder) -> Folder) {
        val folder = folderDao.getById(id)
        val newData = block.invoke(folder)
        update(newData.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun get(id: String): Folder {
        return folderDao.getById(id)
    }

    override fun getAll(): List<Folder> {
        return folderDao.getAll()
    }

    fun getByTag(tagId: Int): List<Folder> {
        return folderDao.getByTag(tagId)
    }
}
