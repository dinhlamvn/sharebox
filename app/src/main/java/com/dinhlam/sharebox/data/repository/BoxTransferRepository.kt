package com.dinhlam.sharebox.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.dinhlam.sharebox.data.local.AppDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxTransferManifest
import com.dinhlam.sharebox.model.BoxTransferShare
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.storage.FirebaseStorageManager
import com.dinhlam.sharebox.utils.FileUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxTransferRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val boxDao: BoxDao,
    private val shareDao: ShareDao,
    private val storageManager: FirebaseStorageManager,
    private val userHelper: UserHelper,
    private val gson: Gson,
) {
    private val shareDataConverter by lazy { ShareDataConverter(gson) }

    suspend fun export(boxId: String): BoxTransferManifest {
        require(userHelper.isSignedIn()) { "Sign in before exporting a box" }
        val box = boxDao.find(boxId) ?: error("Box $boxId was not found")
        require(box.createdBy == userHelper.getCurrentUserId()) {
            "Only the local box owner can export this box"
        }

        val exportedShares = shareDao.findAllInBoxForTransfer(boxId).map { share ->
            val cloudData = uploadLocalAssets(boxId, share.shareId, share.shareData)
            BoxTransferShare(
                shareId = share.shareId,
                shareUserId = share.shareUserId,
                shareData = shareDataConverter.shareDataToString(cloudData),
                isVideoShare = share.isVideoShare,
                shareNote = share.shareNote,
                shareDate = share.shareDate,
                createdAt = share.createdAt,
                updatedAt = share.updatedAt,
            )
        }
        val manifest = BoxTransferManifest(
            boxId = box.boxId,
            boxName = box.boxName,
            boxDesc = box.boxDesc,
            createdBy = box.createdBy,
            createdDate = box.createdDate,
            exportedAt = nowUTCTimeInMillis(),
            shares = exportedShares,
        )

        // The manifest is the commit marker and must be uploaded after every asset.
        storageManager.uploadBoxManifest(boxId, gson.toJson(manifest))
        return manifest
    }

    suspend fun import(boxId: String): Box {
        require(userHelper.isSignedIn()) { "Sign in before importing a box" }
        val manifest = gson.fromJson(
            storageManager.downloadBoxManifest(boxId),
            BoxTransferManifest::class.java,
        )
        require(manifest.schemaVersion == BoxTransferManifest.CURRENT_SCHEMA_VERSION) {
            "Unsupported box manifest schema version: ${manifest.schemaVersion}"
        }
        require(manifest.boxId == boxId) { "Manifest box id does not match $boxId" }

        val currentUserId = userHelper.getCurrentUserId()
        val importedAt = nowUTCTimeInMillis()
        val importedShares = manifest.shares.map { exported ->
            val cloudData = shareDataConverter.stringToShareData(exported.shareData)
            val localData = downloadAssets(boxId, exported.shareId, cloudData)
            Share(
                shareId = exported.shareId,
                shareUserId = currentUserId,
                shareData = localData,
                isVideoShare = exported.isVideoShare,
                shareNote = exported.shareNote,
                shareBoxId = boxId,
                shareDate = exported.shareDate,
                synced = true,
                // Tag ids are device-local and cannot safely cross installations.
                tagId = null,
                createdAt = exported.createdAt,
                updatedAt = exported.updatedAt,
            )
        }
        val existingBox = boxDao.find(boxId)
        val importedBox = Box(
            id = existingBox?.id ?: 0,
            boxId = manifest.boxId,
            boxName = manifest.boxName,
            boxDesc = manifest.boxDesc,
            createdBy = currentUserId,
            createdDate = manifest.createdDate,
            passcode = existingBox?.passcode,
            lastSeen = importedAt,
            synced = true,
            createdAt = existingBox?.createdAt ?: importedAt,
            updatedAt = importedAt,
        )

        appDatabase.withTransaction {
            boxDao.upsert(importedBox)
            shareDao.upsertAll(importedShares)
        }
        return importedBox
    }

    private suspend fun uploadLocalAssets(
        boxId: String,
        shareId: String,
        shareData: ShareData,
    ): ShareData = when (shareData) {
        is ShareData.ShareImage -> shareData.copy(
            uri = uploadIfLocal(boxId, shareId, shareData.uri, 0)
        )
        is ShareData.ShareImages -> shareData.copy(
            uris = shareData.uris.mapIndexed { index, uri ->
                uploadIfLocal(boxId, shareId, uri, index)
            }
        )
        is ShareData.ShareFile -> shareData.copy(
            uri = uploadIfLocal(boxId, shareId, shareData.uri, 0)
        )
        else -> shareData
    }

    private suspend fun uploadIfLocal(
        boxId: String,
        shareId: String,
        uri: Uri,
        index: Int,
    ): Uri = if (FileUtils.isNetworkFile(uri)) {
        uri
    } else {
        storageManager.uploadBoxAsset(boxId, shareId, uri, index)
    }

    private suspend fun downloadAssets(
        boxId: String,
        shareId: String,
        shareData: ShareData,
    ): ShareData = when (shareData) {
        is ShareData.ShareImage -> shareData.copy(
            uri = downloadIfRemote(boxId, shareId, shareData.uri, 0, "image")
        )
        is ShareData.ShareImages -> shareData.copy(
            uris = shareData.uris.mapIndexed { index, uri ->
                downloadIfRemote(boxId, shareId, uri, index, "image")
            }
        )
        is ShareData.ShareFile -> shareData.copy(
            uri = downloadIfRemote(
                boxId,
                shareId,
                shareData.uri,
                0,
                safeFileName(shareData.fileName),
            )
        )
        else -> shareData
    }

    private suspend fun downloadIfRemote(
        boxId: String,
        shareId: String,
        uri: Uri,
        index: Int,
        fileName: String,
    ): Uri {
        if (!FileUtils.isNetworkFile(uri)) {
            return uri
        }
        val destination = File(
            context.filesDir,
            "box-imports/$boxId/$shareId/${index}_$fileName",
        )
        return Uri.fromFile(storageManager.downloadBoxAsset(uri, destination))
    }

    private fun safeFileName(fileName: String): String =
        fileName.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "file" }
}
