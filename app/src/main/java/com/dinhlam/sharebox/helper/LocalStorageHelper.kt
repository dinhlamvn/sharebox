package com.dinhlam.sharebox.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dinhlam.sharebox.extensions.appendIf
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageHelper @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appSettingHelper: AppSettingHelper

) {

    fun cleanUp(sourceUri: Uri) {
        appContext.contentResolver.delete(sourceUri, null, null)
    }

    suspend fun saveVideoToGallery(sourceVideoUri: Uri) =
        withContext(Dispatchers.IO) {
            val resolver = appContext.contentResolver

            val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val newVideo = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "sharebox_video_${FileUtils.getFileNameFromUri(sourceVideoUri)}"
                )
            }

            val destUri =
                resolver.insert(videoCollection, newVideo) ?: return@withContext
            resolver.openInputStream(sourceVideoUri)?.use { inputStream ->
                resolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

    suspend fun saveAutoToGallery(sourceAudioUri: Uri) =
        withContext(Dispatchers.IO) {
            val resolver = appContext.contentResolver

            val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val newAudio = ContentValues().apply {
                put(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    "sharebox_audio_${FileUtils.getFileNameFromUri(sourceAudioUri)}"
                )
            }

            val destUri =
                resolver.insert(audioCollection, newAudio) ?: return@withContext
            resolver.openInputStream(sourceAudioUri)?.use { inputStream ->
                resolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

    suspend fun saveImageToGallery(
        imageSource: Uri, albumName: String? = null
    ) = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val newImage = ContentValues().apply {
            val fileName = FileUtils.getFileNameFromUri(imageSource).appendIf(".jpg") { s ->
                !s.endsWith(".jpg")
            }
            val imageFileName = "sharebox_image_$fileName"
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)

            albumName?.let { album ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                    val fullFinalPath =
                        StringBuilder(Environment.getExternalStorageDirectory().absolutePath)
                            .append(File.separator)
                            .append(Environment.DIRECTORY_PICTURES).append(File.separator)
                            .append(album)
                            .append(File.separator)
                            .append(imageFileName)
                            .toString()
                    File(fullFinalPath).parentFile?.also { file ->
                        if (!file.exists()) {
                            file.mkdirs()
                        }
                    }

                    put(MediaStore.Images.Media.DATA, fullFinalPath)
                } else {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + album
                    )
                }
            }
        }

        val destUri =
            resolver.insert(imageCollection, newImage) ?: return@withContext null

        imageSource.toString().takeIf { uriStr -> uriStr.startsWith("content://") }?.let { uriStr ->
            resolver.openInputStream(Uri.parse(uriStr))?.use { inputStream ->
                resolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } ?: return@withContext ImageLoader.INSTANCE.get(appContext, imageSource)?.let { bitmap ->
            resolver.openOutputStream(destUri)?.use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    appSettingHelper.getImageDownloadQuality(),
                    outputStream
                )
                bitmap.recycle()
            }
        }
    }
}