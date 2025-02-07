package com.dinhlam.sharebox.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.FileUtils

fun Context.getVideoThumbnail(videoUri: Uri): Bitmap? {
    return try {
        if (!FileUtils.isNetworkFile(videoUri)) {
            getVideoThumbnailLocal(videoUri)
        } else {
            getVideoThumbnailNetwork(videoUri)
        }
    } catch (e: Exception) {
        BitmapFactory.decodeResource(resources, R.drawable.mp4)
    }
}

private fun Context.getVideoThumbnailLocal(videoUri: Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(this, videoUri)
    return retriever.getFrameAtTime(1000L)
}

private fun getVideoThumbnailNetwork(videoUri: Uri): Bitmap? {
    return null
}

fun Context.getMimeTypeFromUri(uri: Uri): String? {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(contentResolver.getType(uri))
}

fun Context.getExtensionFromUri(uri: Uri): String? {
    val mimeType = getMimeTypeFromUri(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}