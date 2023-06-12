package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val userRepository: UserRepository,
) {

    suspend fun insert(
        boxId: String,
        boxName: String,
        boxDesc: String?,
        createdBy: String,
        createdDate: Long = nowUTCTimeInMillis(),
        passcode: String? = null,
        lastSeen: Long = nowUTCTimeInMillis()
    ): Box? {
        val box = Box(
            boxId = boxId,
            boxName = boxName,
            boxDesc = boxDesc,
            createdBy = createdBy,
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

    suspend fun findLatestBox(): List<BoxDetail> = boxDao.runCatching {
        findLatestBoxes().asFlow().mapNotNull { box ->
            val userDetail = userRepository.findOne(box.createdBy) ?: return@mapNotNull null
            BoxDetail(
                box.boxId,
                box.boxName,
                box.boxDesc,
                userDetail,
                box.createdDate,
                box.passcode,
                box.lastSeen
            )
        }.toList()
    }.getOrDefault(emptyList())
}