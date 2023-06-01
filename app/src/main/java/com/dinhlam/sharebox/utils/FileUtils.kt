package com.dinhlam.sharebox.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import java.io.File
import java.util.UUID

object FileUtils {

    fun getUriFromFile(context: Context, targetFile: File): Uri {
        return FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.file_provider", targetFile
        )
    }

    fun randomImageFileName(extension: String) = "share_image_${UUID.randomUUID()}.$extension"
}