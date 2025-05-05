package com.dinhlam.sharebox.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.extensions.format
import java.io.File

object FileUtils {

    fun isNetworkFile(uri: Uri): Boolean {
        return uri.toString().run {
            startsWith("http") || startsWith("ftp")
        }
    }

    fun createDownloadFilesDir(context: Context): File? {
        val fileDir =
            context.getExternalFilesDir("download_files") ?: return null
        if (!fileDir.exists() && !fileDir.mkdir()) {
            return null
        }
        return fileDir
    }

    fun createShareFile(context: Context, ext: String): File? {
        val fileDir =
            context.getExternalFilesDir("share_files") ?: return null
        if (!fileDir.exists() && !fileDir.mkdir()) {
            return null
        }

        val file = File(fileDir, createFileName("file", ext))

        if (file.exists() && !file.delete()) {
            return null
        }

        return file.apply { createNewFile() }
    }

    fun createShareImageFile(context: Context, ext: String): File? {
        val imageFileDir =
            context.getExternalFilesDir("share_images") ?: return null
        if (!imageFileDir.exists() && !imageFileDir.mkdir()) {
            return null
        }

        val file = File(imageFileDir, createFileName("image", ext))

        if (file.exists() && !file.delete()) {
            return null
        }

        return file.apply { createNewFile() }
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

    fun createDownloadFile(fileName: String): File? {
        val fileDir = getDownloadDir() ?: return null
        val file = File(fileDir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    private fun getDownloadDir(): File? {
        val downloadPublicDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                ?: return null
        val fileDir = File(downloadPublicDir, "Sharebox")
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            return null
        }
        return fileDir
    }

    fun getFileNameFromUri(uri: Uri) =
        uri.lastPathSegment ?: error("No file name found in uri $uri")

    fun createFileName(prefix: String, ext: String): String {
        return buildString {
            append("sharebox")
            append("_")
            append(prefix)
            append("_")
            append(System.currentTimeMillis().format("yyyyMMdd-HH_mm_ss.SSS"))
            append(".")
            append(ext)
        }
    }
}