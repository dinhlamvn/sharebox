package com.dinhlam.sharebox.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.storage.FirebaseStorageManager
import com.dinhlam.sharebox.utils.FileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncShareToCloudWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val shareRepository: ShareRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val firebaseStorageManager: FirebaseStorageManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        val shareId =
            workerParams.inputData.getString(AppExtras.EXTRA_SHARE_ID) ?: return Result.success()
        val share = shareRepository.findOneRaw(shareId) ?: return Result.success()
        val newShare = when (share.shareData) {
            is ShareData.ShareImage -> uploadShareImage(share, share.shareData)
            is ShareData.ShareImages -> uploadShareImages(share, share.shareData)
            is ShareData.ShareFile -> uploadShareFiles(share, share.shareData)
            else -> share
        }
        realtimeDatabaseRepository.push(newShare)
        return Result.success()
    }

    private suspend fun uploadShareImage(share: Share, shareImage: ShareData.ShareImage): Share {
        val shareUri = shareImage.uri
        if (FileUtils.isNetworkFile(shareUri)) {
            return share
        }
        val fileUri = firebaseStorageManager.uploadFileWithoutNotification(share.shareId, shareUri)
        if (fileUri != null) {
            return share.copy(shareData = shareImage.copy(uri = fileUri))
        }
        return share
    }

    private suspend fun uploadShareImages(share: Share, shareImages: ShareData.ShareImages): Share {
        val shareUris = shareImages.uris.filter(FileUtils::isNetworkFile).toMutableList()
        val uploadUris = shareImages.uris.filterNot(FileUtils::isNetworkFile)
        if (uploadUris.isEmpty()) {
            return share
        }
        repeat(uploadUris.size) { i ->
            val fileUri =
                firebaseStorageManager.uploadFileWithoutNotification(
                    share.shareId,
                    uploadUris[i],
                    i
                )
            if (fileUri != null) {
                shareUris.add(fileUri)
            } else {
                shareUris.add(uploadUris[i])
            }
        }

        return share.copy(shareData = shareImages.copy(uris = shareUris))
    }

    private suspend fun uploadShareFiles(share: Share, shareFile: ShareData.ShareFile): Share {
        val shareUri = shareFile.uri
        if (FileUtils.isNetworkFile(shareUri)) {
            return share
        }
        val fileUri = firebaseStorageManager.uploadFileWithoutNotification(share.shareId, shareUri)
        if (fileUri != null) {
            return share.copy(shareData = shareFile.copy(uri = fileUri))
        }
        return share
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                1912,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_share_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                1912,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_share_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).build()
            )
        }
    }
}