package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dinhlam.sharebox.worker.SyncDataWorker
import com.dinhlam.sharebox.worker.SyncUserDataWorker
import com.dinhlam.sharebox.worker.TiktokVideoDownloadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerUtils {

    private const val TAG_WORKER_SYNC_DATA = "sharebox-worker-sync-data"

    fun enqueueSyncUserData(context: Context) {
        val syncUserDataWorkerRequest =
            OneTimeWorkRequestBuilder<SyncUserDataWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncUserDataWorkerRequest)
    }

    fun enqueueJobSyncData(context: Context) {
        val syncDataWorkerRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            24, TimeUnit.HOURS
        ).addTag(TAG_WORKER_SYNC_DATA).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
        ).build()
        WorkManager.getInstance(context).enqueue(syncDataWorkerRequest)
    }

    fun enqueueJobSyncDataOneTime(context: Context) {
        val syncDataWorkerRequest = OneTimeWorkRequestBuilder<SyncDataWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
        ).build()
        WorkManager.getInstance(context).enqueue(syncDataWorkerRequest)
    }

    fun cancelJobSyncData(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_WORKER_SYNC_DATA)
    }

    fun enqueueJobDownloadTiktokVideo(context: Context, entityId: Int, videoUrl: String) {
        val tiktokDownloadVideoRequest =
            OneTimeWorkRequestBuilder<TiktokVideoDownloadWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).setInputData(
                Data.Builder().putInt("id", entityId).putString("url", videoUrl).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(tiktokDownloadVideoRequest)
    }
}