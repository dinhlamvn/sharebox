package com.dinhlam.sharesaver.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.dinhlam.sharesaver.database.dao.FolderDao
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.di.qualifier.AppSharePref
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    @AppSharePref private val sharePref: SharedPreferences
) : Repository<Folder, String> {
    override fun insert(item: Folder) {
        folderDao.insertAll(item)
    }

    override fun update(item: Folder): Boolean {
        return folderDao.update(item) > 0
    }

    override fun delete(item: Folder): Boolean {
        return folderDao.delete(item) > 0
    }

    override fun insertMany(vararg items: Folder) {
        folderDao.insertAll(*items)
    }

    override fun updateMany(vararg items: Folder) {

    }

    override fun updateById(id: String, block: () -> Folder) {
        update(folderDao.getById(id))
    }

    override fun get(id: String): Folder {
        return folderDao.getById(id)
    }

    override fun getAll(): List<Folder> {
        val firstTime = sharePref.getBoolean("first-run", true)
        if (firstTime) {
            val defaultFolders = listOf(
                Folder("folder_home", "Home", "For all"),
                Folder("folder_text", "Texts", "For plain text share"),
                Folder("folder_url", "Webs", "For web link share"),
                Folder("folder_image", "Images", "For image share")
            )
            folderDao.insertAll(*defaultFolders.toTypedArray())
            sharePref.edit(true) {
                putBoolean("first-run", false)
            }
        }
        return folderDao.getAll()
    }
}