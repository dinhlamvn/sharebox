package com.dinhlam.sharebox.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncShareToCloudWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val shareRepository: ShareRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        val shareId =
            workerParams.inputData.getString(AppExtras.EXTRA_SHARE_ID) ?: return Result.success()
        val share = shareRepository.findOneRaw(shareId) ?: return Result.success()
        realtimeDatabaseRepository.push(share)
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1912,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.sync_data_share_to_cloud))
                .setSubText(appContext.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).build()
        )
    }
}