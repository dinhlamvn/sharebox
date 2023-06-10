package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val userHelper: UserHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) {

    suspend fun insert(
        boxId: String,
        boxName: String,
        boxDesc: String?,
        createdDate: Long = nowUTCTimeInMillis(),
        passcode: String? = null,
        lastSeen: Long = nowUTCTimeInMillis()
    ): Box? {
        val box = Box(
            boxId = boxId,
            boxName = boxName,
            boxDesc = boxDesc,
            createdBy = userHelper.getCurrentUserId(),
            createdDate = createdDate,
            passcode = passcode,
            lastSeen = lastSeen
        )

        return boxDao.runCatching {
            insert(box)
            realtimeDatabaseRepository.push(box)
            box
        }.getOrNull()
    }

    suspend fun findOneRaw(boxId: String): Box? = boxDao.runCatching {
        find(boxId)
    }.getOrNull()

    suspend fun find(): List<Box> = boxDao.runCatching {
        find()
    }.getOrDefault(emptyList())
}