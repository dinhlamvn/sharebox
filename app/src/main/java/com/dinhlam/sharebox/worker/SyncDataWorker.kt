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
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.isServiceRunning
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.services.RealtimeDatabaseService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val SERVICE_ID = 69919090
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.realtime_database_service_noti_content))
                .setSubText(appContext.getString(R.string.realtime_database_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
        )
    }

    override suspend fun doWork(): Result {
        Logger.debug("$this has been started")
        if (appContext.isServiceRunning(RealtimeDatabaseService::class.java.name)) {
            return Result.success()
        }
        setForeground(getForegroundInfo())
        return try {
            realtimeDatabaseRepository.consume()
            var times = 60
            while (times > 0) {
                delay(1_000)
                times--
            }
            Result.success()
        } catch (e: Exception) {
            Result.success()
        } finally {
            realtimeDatabaseRepository.cancel()
        }
    }
}