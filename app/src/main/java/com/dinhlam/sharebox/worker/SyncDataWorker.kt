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
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.router.Router
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
    private val firebaseStorageHelper: FirebaseStorageHelper,
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
            realtimeDatabaseRepository.sync()
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
                realtimeDatabaseRepository.push(share)
                val uris = share.shareData.cast<ShareData.ShareImage>()
                    ?.let { shareImage -> listOf(shareImage.uri) }
                    ?: share.shareData.cast<ShareData.ShareImages>()?.uris ?: emptyList()
                // Do not need upload for network file
                uris.filterNot(FileUtils::isNetworkFile).forEach { uri ->
                    firebaseStorageHelper.uploadShareImageFile(appContext, share.shareId, uri)
                }
            }
        }
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