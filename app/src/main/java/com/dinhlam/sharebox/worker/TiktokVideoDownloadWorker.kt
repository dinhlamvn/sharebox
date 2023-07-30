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
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.extensions.saveFile
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.UserAgentUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jsoup.Jsoup
import java.io.File
import java.net.URLEncoder
import kotlin.random.Random

@HiltWorker
class TiktokVideoDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val videoHelper: VideoHelper,
    private val sssTikServices: SSSTikServices,
    private val localStorageHelper: LocalStorageHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0, true, R.string.download_video_prepare)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            setForeground(createForegroundInfo(0, true, R.string.download_video_prepare))
            val sourceUrl =
                workerParams.inputData.getString("url") ?: return@withContext Result.success()
            val tiktokUrl = videoHelper.getTiktokFullUrl(sourceUrl)
            val videoId =
                Uri.parse(tiktokUrl).lastPathSegment ?: return@withContext Result.success()
            var retryTimes = 3
            var html = ""
            do {
                val encodeUrl = URLEncoder.encode(tiktokUrl, "utf-8")
                val sssTikId = "$encodeUrl&locale=en&tt=azhwU005"
                val requestBody = RequestBody.create(MediaType.parse("text/plain"), "id=$sssTikId")
                val sssTikResponse = sssTikServices.getDownloadLink(
                    UserAgentUtils.pickRandomUserAgent(), requestBody
                )
                if (!sssTikResponse.isSuccessful) {
                    retryTimes--
                    delay(1000)
                    continue
                }
                html = sssTikResponse.body()
                    ?.use { responseBody -> responseBody.use { res -> res.string() } } ?: ""

                if (html.isNotEmpty()) {
                    break
                }

                retryTimes--
                delay(1000)
            } while (retryTimes > 0)

            if (html.isEmpty()) {
                return@withContext Result.success()
            }

            val downloadUrl = parseHtmlSSSTik(html) ?: return@withContext Result.success()

            val outputDir = File(appContext.filesDir, "tiktok_videos")
            if (!outputDir.exists() && !outputDir.mkdir()) {
                return@withContext Result.success()
            }
            val outputFile = File(outputDir, "$videoId.mp4")
            if (outputFile.exists()) {
                outputFile.delete()
            }

            if (!outputFile.createNewFile()) {
                return@withContext Result.success()
            }

            sssTikServices.downloadFile(downloadUrl).saveFile(outputFile) { downloadState ->
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        setForeground(createForegroundInfo(downloadState.progress, false))
                    }

                    is DownloadState.Finished -> {
                        val uri = FileUtils.getUriFromFile(appContext, outputFile)
                        localStorageHelper.saveVideoToGallery(uri)
                        localStorageHelper.cleanUp(uri)
                        showToast(R.string.success_save_video_to_gallery)
                    }

                    is DownloadState.Failed -> {
                        showToast(R.string.error_save_video_to_gallery)
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

    private fun parseHtmlSSSTik(htmlString: String): String? {
        val jsoup = Jsoup.parse(htmlString)
        val aTags = jsoup.getElementsByTag("a")
        return aTags.firstOrNull { element ->
            if (!element.hasAttr("href")) {
                return null
            }
            val href = element.attr("href") ?: ""
            val isMp4Download = element.text().contains("Without watermark")
            href.contains("tikcdn.io") && isMp4Download
        }?.attr("href").takeIfNotNullOrBlank()
    }

    private fun createForegroundInfo(
        progress: Int, indeterminate: Boolean = false,
        @StringRes subTextRes: Int = R.string.download_video_subtext,
    ): ForegroundInfo {
        return ForegroundInfo(
            workerParams.inputData.getInt("id", Random.nextInt()),
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(appContext.getString(subTextRes))
                .setAutoCancel(false)
                .setContentTitle(appContext.getString(R.string.download_video))
                .setSmallIcon(R.drawable.ic_download).setProgress(100, progress, indeterminate)
                .addAction(
                    0,
                    appContext.getString(R.string.cancel),
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}