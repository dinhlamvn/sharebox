package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.BoxDetail
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val userHelper: UserHelper
) : BaseRepository<String, Box>() {

    override suspend fun insertInternal(entity: Box): Box {
        boxDao.insert(entity)
        return entity
    }

    override suspend fun updateInternal(entity: Box, willBeSync: Boolean): Box {
        boxDao.update(entity.copy(synced = !willBeSync))
        return entity
    }

    override suspend fun delete(entity: Box): Boolean {
        boxDao.delete(entity)
        return true
    }

    override suspend fun readOne(id: String): Box? {
        try {
            return boxDao.find(id)
        } catch (e: Exception) {
            Logger.error("Read one record $this from database failed.")
        }
        return null
    }

    override suspend fun count(): Int {
        return boxDao.count(userHelper.getCurrentUserId())
    }

    override suspend fun readAll(): List<Box> {
        try {
            return boxDao.findAll(userHelper.getCurrentUserId())
        } catch (e: Exception) {
            Logger.error("Read all record $this from database failed.")
        }
        return emptyList()
    }

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
        return insert(box)
    }

    suspend fun search(query: String, userId: String): List<BoxDetail> {
        try {
            return boxDao.search(query, userId).asFlow().mapNotNull(::convertBoxToBoxDetail)
                .toList()
        } catch (e: Exception) {
            Logger.error("Search box has error '$query': $e")
        }
        return emptyList()
    }

    suspend fun find(boxIdList: List<String>): List<BoxDetail> {
        try {
            return boxDao.find(boxIdList).asFlow().mapNotNull(::convertBoxToBoxDetail).toList()
        } catch (e: Exception) {
            Logger.error("Find box list with ids $boxIdList has error: $e")
        }
        return emptyList()
    }

    suspend fun find(limit: Int, offset: Int): List<BoxDetail> {
        try {
            return boxDao.find(userHelper.getCurrentUserId(), limit, offset).asFlow()
                .mapNotNull(::convertBoxToBoxDetail).toList()
        } catch (e: Exception) {
            Logger.error("Find box list has error: $e")
        }
        return emptyList()
    }

    suspend fun findOne(boxId: String): BoxDetail? {
        try {
            val box = boxDao.find(boxId) ?: return null
            return convertBoxToBoxDetail(box)
        } catch (e: Exception) {
            Logger.error("Find box with id $boxId has error: $e")
        }
        return null
    }

    suspend fun findOneRaw(boxId: String): Box? {
        try {
            return boxDao.find(boxId)
        } catch (e: Exception) {
            Logger.error("Find box with id $boxId has error: $e")
        }
        return null
    }

    private fun convertBoxToBoxDetail(box: Box): BoxDetail {
        return BoxDetail(
            box.boxId,
            box.boxName,
            box.boxDesc,
            box.createdBy,
            box.createdDate,
            box.passcode,
            box.lastSeen
        )
    }

    suspend fun findForSyncToCloud(): List<Box> {
        try {
            return boxDao.findForSyncToCloud()
        } catch (e: Exception) {
            Logger.error("Find box to sync to cloud has error: $e")
        }
        return emptyList()
    }

    suspend fun transferData(anonymousUserId: String, userId: String) {
        boxDao.transferData(anonymousUserId, userId)
    }

    suspend fun findLastActiveBox(): BoxDetail? {
        try {
            val box =
                boxDao.findLatestBoxWithoutPasscode(userHelper.getCurrentUserId()) ?: return null
            return convertBoxToBoxDetail(box)
        } catch (e: Exception) {
            Logger.error("Find last active box has error: $e")
        }
        return null
    }
}