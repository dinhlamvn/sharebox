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
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.utils.FileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

@HiltWorker
class DownloadImagesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val sssTikServices: SSSTikServices,
    private val localStorageHelper: LocalStorageHelper
) : CoroutineWorker(appContext, workerParams) {

    private val notificationId = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(notificationId, 0, 0)
    }

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(notificationId, 0, 0))
        return withContext(Dispatchers.IO) {
            val id = workerParams.inputData.getString(AppExtras.EXTRA_ID)
            val urls = workerParams.inputData.getStringArray(AppExtras.EXTRA_DOWNLOAD_IMAGES)
                ?: emptyArray()
            val outputDir =
                FileUtils.createShareImagesDir(appContext) ?: return@withContext Result.success()
            if (!outputDir.exists() && !outputDir.mkdir()) {
                return@withContext Result.failure()
            }

            val size = urls.size
            val albumName = "sharebox_images_$id"
            var downloaded = 0

            urls.forEachIndexed { index, url ->
                val outputFile =
                    File(outputDir, "sharebox_image_${id}_${System.currentTimeMillis()}_$index.jpg")
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                try {
                    if (url.startsWith("content://")) {
                        appContext.contentResolver.openInputStream(Uri.parse(url))?.use { ips ->
                            outputFile.outputStream().use { os ->
                                ips.copyTo(os)
                                val uri = FileUtils.getUriFromFile(appContext, outputFile)
                                localStorageHelper.saveImageToGallery(uri, albumName)
                                localStorageHelper.cleanUp(uri)
                                setForeground(createForegroundInfo(notificationId, size, ++downloaded))
                            }
                        }
                    } else {
                        sssTikServices.downloadFile(url).use { body ->
                            body.byteStream().use { bs ->
                                outputFile.outputStream().use { os ->
                                    bs.copyTo(os)
                                    val uri = FileUtils.getUriFromFile(appContext, outputFile)
                                    localStorageHelper.saveImageToGallery(uri, albumName)
                                    localStorageHelper.cleanUp(uri)
                                    setForeground(createForegroundInfo(notificationId, size, ++downloaded))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            appContext,
                            R.string.error_save_image_to_gallery,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            if (downloaded > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        appContext,
                        R.string.success_save_image_to_gallery,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Result.success()
        }
    }

    private fun createForegroundInfo(
        id: Int,
        size: Int, downloaded: Int
    ): ForegroundInfo {
        return ForegroundInfo(
            id,
            NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                .setContentText(
                    appContext.getString(
                        R.string.downloading_all_image,
                        downloaded,
                        size
                    )
                )
                .setAutoCancel(false)
                .setContentTitle(appContext.getString(R.string.downloading))
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(
                    0,
                    appContext.getString(R.string.cancel),
                    WorkManager.getInstance(appContext).createCancelPendingIntent(workerParams.id)
                ).build()
        )
    }
}