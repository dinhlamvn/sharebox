package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.worker.DirectDownloadShareWorker
import com.dinhlam.sharebox.worker.SyncDataWorker
import com.dinhlam.sharebox.worker.SyncShareToCloudWorker
import com.dinhlam.sharebox.worker.SyncUserDataWorker
import com.dinhlam.sharebox.worker.TiktokDownloadWorker
import com.dinhlam.sharebox.worker.YoutubeDownloadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerUtils {

    private const val TAG_WORKER_SYNC_DATA = "sharebox-worker-sync-data"
    private const val TAG_WORKER_SYNC_DATA_ONE_TIME = "sharebox-worker-sync-data-one-time"

    private fun getWorkerSyncDataUUID(): UUID =
        UUID.nameUUIDFromBytes(TAG_WORKER_SYNC_DATA.toByteArray())

    private fun getWorkerSyncDataOneTimeUUID(): UUID =
        UUID.nameUUIDFromBytes(TAG_WORKER_SYNC_DATA.toByteArray())

    fun enqueueSyncUserData(context: Context) {
        val syncUserDataWorkerRequest =
            OneTimeWorkRequestBuilder<SyncUserDataWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncUserDataWorkerRequest)
    }

    fun enqueueJobSyncData(context: Context) {
        val syncDataWorkerRequest =
            PeriodicWorkRequestBuilder<SyncDataWorker>(6, TimeUnit.HOURS).setId(
                getWorkerSyncDataUUID()
            ).setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncDataWorkerRequest)
    }

    fun enqueueJobSyncDataOneTime(context: Context) {
        val syncDataWorkerRequest = OneTimeWorkRequestBuilder<SyncDataWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()
        WorkManager.getInstance(context).enqueue(syncDataWorkerRequest)
    }

    fun cancelJobSyncData(context: Context) {
        WorkManager.getInstance(context).cancelWorkById(getWorkerSyncDataUUID())
    }

    fun enqueueJobDownloadTiktokVideo(context: Context, entityId: Int, videoUrl: String) {
        val tiktokDownloadRequest =
            OneTimeWorkRequestBuilder<TiktokDownloadWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).setInputData(
                Data.Builder().putInt("id", entityId).putString("url", videoUrl).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(tiktokDownloadRequest)
    }

    fun enqueueJobDownloadYoutube(context: Context, entityId: Int, videoUrl: String) {
        val youtubeDownloadRequest =
            OneTimeWorkRequestBuilder<YoutubeDownloadWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).setInputData(
                Data.Builder().putInt("id", entityId).putString("url", videoUrl).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(youtubeDownloadRequest)
    }

    fun enqueueSyncShareToCloud(context: Context, shareId: String) {
        val syncShareToCloudRequest =
            OneTimeWorkRequestBuilder<SyncShareToCloudWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setInputData(
                Data.Builder().putString(AppExtras.EXTRA_SHARE_ID, shareId).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(syncShareToCloudRequest)
    }

    fun enqueueDownloadShare(context: Context, shareUrl: String?) {
        val downloadShareRequest =
            OneTimeWorkRequestBuilder<DirectDownloadShareWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).setInputData(
                Data.Builder().putString(AppExtras.EXTRA_URL, shareUrl).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(downloadShareRequest)
    }
}