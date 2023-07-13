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
import com.dinhlam.sharebox.logger.Logger
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
        private const val SERVICE_ID = 699190901
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        Logger.debug("$this has been started")
        setForeground(getForegroundInfo())
        return try {
            var currentTime = 0
            realtimeDatabaseRepository.consume()
            while (!realtimeDatabaseRepository.isDone() && currentTime < 5 * 60 * 1000) {
                delay(600)
                currentTime += 600
            }
            Result.success()
        } catch (e: Exception) {
            Result.success()
        } finally {
            realtimeDatabaseRepository.cancel()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            SERVICE_ID,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                .setContentText(appContext.getString(R.string.realtime_database_service_noti_content))
                .setContentTitle(appContext.getString(R.string.realtime_database_service_noti_subtext))
                .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                    PendingIntent.getActivity(
                        appContext,
                        0,
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).setProgress(100, 0, true).build()
        )
    }
}