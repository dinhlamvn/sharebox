package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.TagDao
import com.dinhlam.sharebox.data.local.entity.Tag
import com.dinhlam.sharebox.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) : BaseRepository<Int, Tag>() {

    override suspend fun insertInternal(entity: Tag): Tag {
        return entity
    }

    override suspend fun updateInternal(entity: Tag, willBeSync: Boolean): Tag {
        return entity
    }

    override suspend fun readAll(): List<Tag> {
        try {
            return tagDao.find()
        } catch (e: Exception) {
            Logger.error("Read all record $this from database failed.")
        }
        return emptyList()
    }

    override suspend fun readOne(id: Int): Tag? {
        try {
            return tagDao.find(id)
        } catch (e: Exception) {
            Logger.error("Read one record $this from database failed.")
        }
        return null
    }

    override suspend fun count(): Int {
        return 0
    }

    override suspend fun delete(entity: Tag): Boolean {
        return false
    }
}