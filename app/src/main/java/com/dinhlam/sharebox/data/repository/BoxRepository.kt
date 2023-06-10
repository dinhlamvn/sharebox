package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.entity.Box
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) {

    suspend fun findOneRaw(boxId: String): Box? = boxDao.runCatching {
        find(boxId)
    }.getOrNull()
}