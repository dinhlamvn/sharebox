package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.worker.DownloadImagesWorker
import com.dinhlam.sharebox.worker.ExportBoxWorker
import com.dinhlam.sharebox.worker.ImportBoxWorker
import com.dinhlam.sharebox.worker.SyncDataWorker
import com.dinhlam.sharebox.worker.SyncShareToCloudWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerUtils {

    private const val TAG_WORKER_SYNC_DATA = "sharebox-worker-sync-data"

    private fun getWorkerSyncDataUUID(): UUID =
        UUID.nameUUIDFromBytes(TAG_WORKER_SYNC_DATA.toByteArray())

    fun enqueueJobSyncDataEveryDay(context: Context) = cancelJobSyncData(context)

    fun enqueueJobSyncDataOneTime(context: Context) = Unit

    fun cancelJobSyncData(context: Context) {
        WorkManager.getInstance(context).cancelWorkById(getWorkerSyncDataUUID())
    }

    fun enqueueSyncShareToCloud(context: Context, shareId: String) = Unit

    fun enqueueDownloadImages(context: Context, id: String, urls: List<String>) {
        val imageDownloadRequest = OneTimeWorkRequestBuilder<DownloadImagesWorker>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true).setRequiresBatteryNotLow(true).build()
        ).setInputData(
            Data.Builder().putString(AppExtras.EXTRA_ID, id)
                .putStringArray(AppExtras.EXTRA_DOWNLOAD_IMAGES, urls.toTypedArray()).build()
        ).setId(UUID.randomUUID()).build()
        WorkManager.getInstance(context).enqueue(imageDownloadRequest)
    }

    fun enqueueExportBox(context: Context, boxId: String): UUID {
        val request = OneTimeWorkRequestBuilder<ExportBoxWorker>()
            .setConstraints(networkConstraints())
            .setInputData(Data.Builder().putString(AppExtras.EXTRA_BOX_ID, boxId).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "sharebox-export-$boxId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return request.id
    }

    fun enqueueImportBox(context: Context, boxId: String): UUID {
        val request = OneTimeWorkRequestBuilder<ImportBoxWorker>()
            .setConstraints(networkConstraints())
            .setInputData(Data.Builder().putString(AppExtras.EXTRA_BOX_ID, boxId).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "sharebox-import-$boxId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return request.id
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresStorageNotLow(true)
        .build()
}
