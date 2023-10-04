package com.dinhlam.sharebox.worker

import android.app.PendingIntent
import android.content.Context
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
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.Router
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val boxRepository: BoxRepository,
    private val shareRepository: ShareRepository,
    private val router: Router,
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
            delay(3_000)
            Result.success()
        } catch (e: Exception) {
            Result.success()
        } finally {
            realtimeDatabaseRepository.onDestroy()
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
            }
        }
    }


    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.sync_data_to_cloud))
                .setSubText(appContext.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_cloud_upload).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        appContext, 1122, router.settingIntent(), PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        )
    }
}