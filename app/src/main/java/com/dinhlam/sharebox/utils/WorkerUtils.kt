package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dinhlam.sharebox.worker.CleanUpDataWorker
import com.dinhlam.sharebox.worker.SyncDataWorker
import com.dinhlam.sharebox.worker.SyncUserDataWorker
import com.dinhlam.sharebox.worker.SyncVideosWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    fun enqueueCleanUpOldData(context: Context) {
        val cleanUpDataWorkerRequest = OneTimeWorkRequestBuilder<CleanUpDataWorker>().build()
        WorkManager.getInstance(context).enqueue(cleanUpDataWorkerRequest)
    }

    fun enqueueSyncUserData(context: Context) {
        val syncUserDataWorkerRequest =
            OneTimeWorkRequestBuilder<SyncUserDataWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncUserDataWorkerRequest)
    }

    fun enqueueJobSyncData(context: Context) {
        val syncDataWorkerRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            1,
            TimeUnit.HOURS
        ).addTag("sb-worker-sync-data").setConstraints(
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

    fun enqueueJobSyncVideosOneTime(context: Context) {
        val syncVideosWorkerRequest = OneTimeWorkRequestBuilder<SyncVideosWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncVideosWorkerRequest)
    }

    fun cancelJobSyncData(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("sb-worker-sync-data")
    }

    fun cancelJobSyncVideoSchedule(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("worker-sync-video-schedule")
    }
}