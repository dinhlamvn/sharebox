package com.dinhlam.sharebox.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object FileUtils {

    suspend fun copyVideoToExternalStorage(context: Context, sourceVideoUri: Uri) =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver

            val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val newVideo = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "sharebox_video_${getFileNameFromUri(sourceVideoUri)}"
                )
            }

            val destUri =
                resolver.insert(videoCollection, newVideo) ?: throw Exception("Dest uri is null")
            resolver.openInputStream(sourceVideoUri)?.use { inputStream ->
                resolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

    fun createShareImagesDir(context: Context): File? {
        val imageFileDir =
            context.getExternalFilesDir("share_images") ?: return null
        if (!imageFileDir.exists() && !imageFileDir.mkdir()) {
            return null
        }
        return imageFileDir
    }

    fun getUriFromFile(context: Context, targetFile: File): Uri {
        return FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.file_provider", targetFile
        )
    }

    fun isFileExistedFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun randomImageFileName(extension: String) = "share_image_${UUID.randomUUID()}.$extension"

    fun getFileNameFromUri(uri: Uri) =
        uri.lastPathSegment ?: error("No file name found in uri $uri")
}