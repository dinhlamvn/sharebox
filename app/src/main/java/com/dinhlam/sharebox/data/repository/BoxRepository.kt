package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.model.BoxDetail
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val userRepository: UserRepository,
) {

    suspend fun insert(
        boxId: String,
        boxName: String,
        boxDesc: String?,
        createdBy: String,
        createdDate: Long = nowUTCTimeInMillis(),
        passcode: String? = null,
        lastSeen: Long = nowUTCTimeInMillis(),
        synced: Boolean = false,
    ): Box? {
        val box = Box(
            boxId = boxId,
            boxName = boxName,
            boxDesc = boxDesc,
            createdBy = createdBy,
            createdDate = createdDate,
            passcode = passcode,
            lastSeen = lastSeen,
            synced = synced
        )

        return boxDao.runCatching {
            insert(box)
            box
        }.getOrNull()
    }

    suspend fun update(box: Box): Boolean = boxDao.runCatching {
        update(box)
        true
    }.getOrDefault(false)

    suspend fun updateLastSeen(boxId: String, lastSeen: Long) {
        val box = boxDao.find(boxId) ?: return
        val newBox = box.copy(lastSeen = lastSeen)
        boxDao.update(newBox)
    }

    suspend fun search(query: String): List<BoxDetail> = boxDao.runCatching {
        search(query).asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun find(limit: Int, offset: Int): List<BoxDetail> = boxDao.runCatching {
        find(limit, offset).asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findByUser(userId: String, limit: Int, offset: Int): List<BoxDetail> =
        boxDao.runCatching {
            find(userId, limit, offset).asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
        }.getOrDefault(emptyList())

    suspend fun count(): Int = boxDao.runCatching {
        count()
    }.getOrDefault(0)

    suspend fun findOne(boxId: String): BoxDetail? = boxDao.runCatching {
        val box = find(boxId) ?: return@runCatching null
        convertBoxToBoxDetail(box)
    }.getOrNull()

    suspend fun findOneRaw(boxId: String): Box? = boxDao.runCatching {
        find(boxId)
    }.getOrNull()

    suspend fun findLatestBox(): List<BoxDetail> = boxDao.runCatching {
        findLatestBoxes().asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
    }.getOrDefault(emptyList())

    suspend fun findLatestBoxWithoutPasscode(): List<BoxDetail> = boxDao.runCatching {
        findLatestBoxesWithoutPasscode().asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
    }.getOrDefault(emptyList())

    private suspend fun convertBoxToBoxDetail(box: Box): BoxDetail? {
        val userDetail = userRepository.findOne(box.createdBy) ?: return null
        return BoxDetail(
            box.boxId,
            box.boxName,
            box.boxDesc,
            userDetail,
            box.createdDate,
            box.passcode,
            box.lastSeen
        )
    }

    suspend fun findForSyncToCloud(): List<Box> = boxDao.runCatching {
        findForSyncToCloud()
    }.getOrDefault(emptyList())
}