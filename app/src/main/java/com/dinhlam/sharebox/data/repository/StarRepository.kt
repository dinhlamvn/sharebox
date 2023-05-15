package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.StarDao
import com.dinhlam.sharebox.data.local.entity.Star
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarRepository @Inject constructor(
    private val starDao: StarDao
) {

    fun starred(shareId: String): Boolean = starDao.runCatching {
        insert(Star(shareId = shareId))
        true
    }.getOrDefault(false)

    fun unStarred(shareId: String): Boolean = starDao.runCatching {
        delete(shareId)
        true
    }.getOrDefault(false)

    fun findOne(shareId: String): Star? = starDao.runCatching {
        find(shareId)
    }.getOrDefault(null)

    fun find() = starDao.runCatching {
        find()
    }.getOrDefault(emptyList())
}