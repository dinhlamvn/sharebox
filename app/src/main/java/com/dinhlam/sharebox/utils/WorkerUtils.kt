package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dinhlam.sharebox.worker.CleanUpDataWorker
import com.dinhlam.sharebox.worker.SyncUserDataWorker

object WorkerUtils {

    fun enqueueCleanUpOldData(context: Context) {
        val cleanUpDataWorkerRequest = OneTimeWorkRequestBuilder<CleanUpDataWorker>().build()
        WorkManager.getInstance(context).enqueue(cleanUpDataWorkerRequest)
    }

    fun enqueueSyncUserData(context: Context) {
        val syncUserDataWorkerRequest =
            OneTimeWorkRequestBuilder<SyncUserDataWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(false).build()
            ).build()
        WorkManager.getInstance(context).enqueue(syncUserDataWorkerRequest)
    }
}