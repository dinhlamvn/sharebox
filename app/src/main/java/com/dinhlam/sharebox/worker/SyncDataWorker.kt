package com.dinhlam.sharebox.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
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
            realtimeDatabaseRepository.consume()

            var time = 0

            while (time < 10 * 600) {
                time += 10
                delay(1_000)
            }

            notifyDataSyncSuccess()
            Result.success()
        } catch (e: Exception) {
            Result.success()
        } finally {
            realtimeDatabaseRepository.onDestroy()
        }
    }


    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.sync_data_in_background_text))
                .setSubText(appContext.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false)
                .setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        1,
                        router.settingIntent(),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
        )
    }

    private fun notifyDataSyncSuccess() {
        val notification =
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.notify_data_sync_success_text))
                .setContentTitle(appContext.getString(R.string.notify_data_sync_success_title))
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false)
                .setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        val notificationManager =
            appContext.getSystemServiceCompat<NotificationManager>(Context.NOTIFICATION_SERVICE)
        notificationManager.notify(123113, notification)
    }
}