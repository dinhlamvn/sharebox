package com.dinhlam.sharebox.worker

import android.app.PendingIntent
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
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.storage.FirebaseStorageManager
import com.dinhlam.sharebox.utils.FileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
    private val router: Router,
    private val firebaseStorageManager: FirebaseStorageManager,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val SERVICE_ID = 699190901
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        Logger.debug("$this has been started")
        setForeground(getForegroundInfo())
        return try {
            syncBoxes()
            syncShares()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncBoxes() {
        while (true) {
            val boxes = boxRepository.findForSyncToCloud()
            if (boxes.isEmpty()) {
                break
            }
            boxes.forEach { box ->
                realtimeDatabaseRepository.push(box)
            }
        }
    }

    private suspend fun syncShares() {
        while (true) {
            val shares = shareRepository.findForSyncToCloud()
            if (shares.isEmpty()) {
                break
            }
            shares.forEach { share ->
                val newShare = when (share.shareData) {
                    is ShareData.ShareImage -> handleShareImage(share, share.shareData)
                    is ShareData.ShareImages -> handleShareImages(share, share.shareData)
                    else -> share
                }
                realtimeDatabaseRepository.push(newShare)
            }
        }
    }

    private suspend fun handleShareImage(share: Share, shareImage: ShareData.ShareImage): Share {
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

    private suspend fun handleShareImages(share: Share, shareImages: ShareData.ShareImages): Share {
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


    private fun createForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                SERVICE_ID,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_cloud_upload).setAutoCancel(false).setContentIntent(
                        PendingIntent.getActivity(
                            appContext, 1122, router.setting(), PendingIntent.FLAG_IMMUTABLE
                        )
                    ).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                SERVICE_ID,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_cloud_upload).setAutoCancel(false).setContentIntent(
                        PendingIntent.getActivity(
                            appContext, 1122, router.setting(), PendingIntent.FLAG_IMMUTABLE
                        )
                    ).build()
            )
        }
    }
}