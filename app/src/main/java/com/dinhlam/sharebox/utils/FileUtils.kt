package com.dinhlam.sharebox.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import java.io.File
import java.util.UUID

object FileUtils {

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