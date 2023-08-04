package com.dinhlam.sharebox.worker

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.extensions.saveFile
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

@HiltWorker
class YoutubeMp3DownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val libreTubeServices: LibreTubeServices,
    private val localStorageHelper: LocalStorageHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0, true, appContext.getString(R.string.download_preparing))
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(
                createForegroundInfo(
                    0,
                    true,
                    appContext.getString(R.string.download_preparing)
                )
            )
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.success()
            val videoId =
                Uri.parse(sourceUrl).getQueryParameter("v") ?: return@withContext Result.success()

            val responseBody =
                libreTubeServices.getDownloadLink(UserAgentUtils.pickRandomUserAgent(), videoId)

            val strResponse = responseBody.body()?.use { res -> res.string() }
                ?: return@withContext Result.success()

            val json = JSONObject(strResponse)
            val audioStreams = json.getJSONArray("audioStreams") ?: JSONArray()

            if (audioStreams.length() == 0) {
                return@withContext Result.success()
            }

            val firstMp3 = audioStreams.getJSONObject(0)
            val url = firstMp3.getString("url") ?: return@withContext Result.success()
            val contentLength = firstMp3.getLong("contentLength")
            val title = json.getString("title")

            val outputDir = File(appContext.filesDir, "youtube_mp3")
            if (!outputDir.exists() && !outputDir.mkdir()) {
                return@withContext Result.success()
            }
            val outputFile = File(outputDir, "$videoId.mp3")
            if (outputFile.exists()) {
                outputFile.delete()
            }

            if (!outputFile.createNewFile()) {
                return@withContext Result.success()
            }

            libreTubeServices.downloadFile(url)
                .saveFile(outputFile, contentLength) { downloadState ->
                    when (downloadState) {
                        is DownloadState.Downloading -> {
                            setForeground(
                                createForegroundInfo(
                                    downloadState.progress,
                                    false,
                                    title
                                )
                            )
                        }

                        is DownloadState.Finished -> {
                            val uri = FileUtils.getUriFromFile(appContext, outputFile)
                            localStorageHelper.saveAutoToGallery(uri)
                            localStorageHelper.cleanUp(uri)
                            showToast(R.string.success_save_audio_to_gallery)
                        }

                        is DownloadState.Failed -> {
                            showToast(R.string.error_save_audio_to_gallery)
                        }
                    }
                }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun showToast(@StringRes strRes: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(appContext, strRes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createForegroundInfo(
        progress: Int, indeterminate: Boolean = false,
        subText: String,
    ): ForegroundInfo {
        return ForegroundInfo(
            workerParams.inputData.getInt("id", Random.nextInt()),
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(subText)
                .setAutoCancel(false)
                .setContentTitle(appContext.getString(R.string.downloading))
                .setSmallIcon(R.drawable.ic_download).setProgress(100, progress, indeterminate)
                .addAction(
                    0,
                    appContext.getString(R.string.cancel),
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}