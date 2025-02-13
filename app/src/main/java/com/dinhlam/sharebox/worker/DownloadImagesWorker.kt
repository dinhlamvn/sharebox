package com.dinhlam.sharebox.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.storage.LocalStorageManager
import com.dinhlam.sharebox.utils.FileUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class DownloadImagesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val localStorageManager: LocalStorageManager,
    private val downloadServices: DownloadServices
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

            if (urls.isEmpty()) {
                return@withContext Result.success()
            }

            val size = urls.size
            val albumName = "sharebox_images_$id"
            var downloaded = 0

            urls.forEach { url ->
                val outputFile = FileUtils.createShareImageFile(appContext, "jpg")
                    ?: return@withContext Result.success()
                try {
                    if (url.startsWith("content://")) {
                        appContext.contentResolver.openInputStream(Uri.parse(url))?.use { ips ->
                            outputFile.outputStream().use { os ->
                                ips.copyTo(os)
                                val uri = FileUtils.getUriFromFile(appContext, outputFile)
                                localStorageManager.saveImageToGallery(uri, albumName)
                                localStorageManager.cleanUp(uri)
                                setForeground(
                                    createForegroundInfo(
                                        notificationId,
                                        size,
                                        ++downloaded
                                    )
                                )
                            }
                        }
                    } else {
                        downloadServices.downloadFile(url).use { body ->
                            body.byteStream().use { bs ->
                                outputFile.outputStream().use { os ->
                                    bs.copyTo(os)
                                    val uri = FileUtils.getUriFromFile(appContext, outputFile)
                                    localStorageManager.saveImageToGallery(uri, albumName)
                                    localStorageManager.cleanUp(uri)
                                    setForeground(
                                        createForegroundInfo(
                                            notificationId,
                                            size,
                                            ++downloaded
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        appContext.showToast(R.string.error_save_image_to_gallery)
                    }
                }
            }

            if (downloaded > 0) {
                appContext.showToast(R.string.success_save_image_to_gallery)
                withContext(Dispatchers.Main) {
                    appContext.showToast(R.string.success_save_image_to_gallery)
                }
            }
            Result.success()
        }
    }

    private fun createForegroundInfo(
        id: Int,
        size: Int, downloaded: Int
    ): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
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
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
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
                        WorkManager.getInstance(appContext)
                            .createCancelPendingIntent(workerParams.id)
                    ).build()
            )
        }
    }
}