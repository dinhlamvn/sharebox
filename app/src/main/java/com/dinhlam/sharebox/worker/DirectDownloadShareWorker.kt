package com.dinhlam.sharebox.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.helper.VideoHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@HiltWorker
class DirectDownloadShareWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        try {
            val shareUrl =
                workerParams.inputData.getString(AppExtras.EXTRA_URL) ?: error("No share url")
            val originUrl = getOriginUrl(shareUrl)
            val videoSource = videoHelper.getVideoSource(originUrl) ?: error("No video source")
            val videoOriginUrl =
                videoHelper.getVideoOriginUrl(videoSource, originUrl) ?: error("No video url")
            videoHelper.downloadVideo(appContext, notificationId, videoSource, videoOriginUrl)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, R.string.nothing_to_download, Toast.LENGTH_SHORT).show()
            }
        }
        return Result.success()
    }

    private suspend fun getOriginUrl(s: String): String = withContext(Dispatchers.IO) {
        val call = okHttpClient.newCall(Request.Builder().url(s).build())
        val body = call.execute()

        if (body.isRedirect) {
            return@withContext body.use { responseBody -> responseBody.header("Location")!! }
        }

        val url = body.use { responseBody -> responseBody.request.url.toString() }

        if (url.contains("login.php?next=")) {
            Uri.parse(url).getQueryParameter("next")!!
        } else {
            url
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                notificationId,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(
                        appContext.getString(
                            R.string.download_preparing,
                            workerParams.inputData.getString(AppExtras.EXTRA_URL)
                        )
                    )
                    .setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                notificationId,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentText(
                        appContext.getString(
                            R.string.download_preparing,
                            workerParams.inputData.getString(AppExtras.EXTRA_URL)
                        )
                    )
                    .setAutoCancel(false)
                    .setContentTitle(appContext.getString(R.string.downloading))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setContentIntent(
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build()
            )
        }
    }
}