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
import com.dinhlam.sharebox.worker.SyncVideoWorker
import com.dinhlam.sharebox.worker.TiktokVideoDownloadWorker
import com.dinhlam.sharebox.worker.YoutubeMp3DownloadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerUtils {

    private const val TAG_WORKER_SYNC_DATA = "sharebox-worker-sync-data"
    private const val TAG_WORKER_SYNC_VIDEO = "sharebox-worker-sync-video"

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

    fun enqueueJobDownloadYoutubeMp3(context: Context, entityId: Int, videoUrl: String) {
        val youtubeDownloadMp3Request =
            OneTimeWorkRequestBuilder<YoutubeMp3DownloadWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).setInputData(
                Data.Builder().putInt("id", entityId).putString("url", videoUrl).build()
            ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(youtubeDownloadMp3Request)
    }

    fun enqueueJobSyncVideoPeriodic(context: Context) {
        val syncVideoWorkerRequest = PeriodicWorkRequestBuilder<SyncVideoWorker>(
            1, TimeUnit.HOURS
        ).addTag(TAG_WORKER_SYNC_VIDEO).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
        ).build()
        WorkManager.getInstance(context).enqueue(syncVideoWorkerRequest)
    }
}