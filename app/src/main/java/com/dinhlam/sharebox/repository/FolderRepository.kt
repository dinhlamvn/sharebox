package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.FolderDao
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.model.SortType
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val folderDao: FolderDao
) : Repository<Folder, String> {
    override fun insert(data: Folder) {
        folderDao.insertAll(data)
    }

    override fun update(data: Folder): Boolean {
        return folderDao.update(data.copy(updatedAt = System.currentTimeMillis())) > 0
    }

    override fun delete(data: Folder): Boolean {
        return folderDao.delete(data) > 0
    }

    override fun insertMany(vararg items: Folder) {
        folderDao.insertAll(*items)
    }

    override fun updateMany(vararg items: Folder) {
    }

    override fun updateById(id: String, block: (Folder) -> Folder) {
        val folder = folderDao.getById(id) ?: return
        val newData = block.invoke(folder)
        update(newData.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun find(id: String): Folder? {
        return folderDao.getById(id)
    }

    override fun findAll(sortType: SortType): List<Folder> {
        return when (sortType) {
            SortType.NEWEST -> folderDao.getAllNewest()
            SortType.OLDEST -> folderDao.getAllOldest()
            else -> folderDao.getAll()
        }
    }

    fun getByTag(tagId: Int): List<Folder> {
        return folderDao.getByTag(tagId)
    }

    override fun search(query: String): List<Folder> {
        return emptyList()
    }

    override fun rowCount(): Int {
        return folderDao.count()
    }
}
