package com.dinhlam.sharebox.worker

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

@HiltWorker
class YoutubeDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val libreTubeServices: LibreTubeServices,
    private val localStorageHelper: LocalStorageHelper,
    private val router: Router,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(appContext.getString(R.string.download_preparing))
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(
                createForegroundInfo(
                    appContext.getString(R.string.download_preparing)
                )
            )
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.success()
            val videoId =
                Uri.parse(sourceUrl).getQueryParameter("v") ?: return@withContext Result.success()

            val responseBody =
                libreTubeServices.getDownloadLink(UserAgentUtils.pickRandomUserAgent(), videoId)

            val strResponse =
                responseBody.body()?.use { res -> res.string() } ?: return@withContext withContext(
                    Dispatchers.Main
                ) {
                    Toast.makeText(appContext, R.string.download_failed, Toast.LENGTH_SHORT).show()
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }

            val json = JSONObject(strResponse)
            val videoStreams = json.getJSONArray("videoStreams") ?: JSONArray()
            val audioStreams = json.getJSONArray("audioStreams") ?: JSONArray()

            if (videoStreams.length() == 0 && audioStreams.length() == 0) {
                Toast.makeText(appContext, R.string.nothing_to_download, Toast.LENGTH_SHORT).show()
                return@withContext Result.success()
            }

            val videos = mutableListOf<DownloadData>()
            for (i in 0 until videoStreams.length()) {
                val videoObj = videoStreams.getJSONObject(i)
                val mimeType = videoObj.getString("mimeType")
                if (mimeType.contains("video/mp4", true)) {
                    videos.add(
                        DownloadData(
                            videoId,
                            mimeType,
                            "(${videoObj.getString("quality")})",
                            videoObj.getString("url")
                        )
                    )
                }
            }

            val audios = mutableListOf<DownloadData>()
            for (i in 0 until audioStreams.length()) {
                val audioObject = audioStreams.getJSONObject(i)
                val mimeType = audioObject.getString("mimeType")
                if (mimeType.contains("audio/mp3", true) || mimeType.contains("audio/mp4", true)) {
                    audios.add(
                        DownloadData(
                            videoId,
                            mimeType,
                            "(${audioObject.getString("quality")})",
                            audioObject.getString("url")
                        )
                    )
                }

            }

            val intent = router.downloadPopup(appContext, videos, audios, emptyList())
            appContext.startActivity(intent)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createForegroundInfo(subText: String): ForegroundInfo {
        return ForegroundInfo(
            workerParams.inputData.getInt("id", Random.nextInt()),
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(subText).setAutoCancel(false)
                .setContentTitle(appContext.getString(R.string.downloading))
                .setSmallIcon(R.mipmap.ic_launcher).addAction(
                    0,
                    appContext.getString(R.string.cancel),
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}