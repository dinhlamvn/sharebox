package com.dinhlam.sharebox.extensions

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.dinhlam.sharebox.utils.FileUtils
import wseemann.media.FFmpegMediaMetadataRetriever

fun Context.getVideoThumbnail(videoUri: Uri): Bitmap? {
    return try {
        if (!FileUtils.isNetworkFile(videoUri)) {
            getVideoThumbnailLocal(videoUri)
        } else {
            getVideoThumbnailNetwork(videoUri)
        }
    } catch (e: Exception) {
        null
    }
}

private fun Context.getVideoThumbnailLocal(videoUri: Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(this, videoUri)
    return retriever.getFrameAtTime(1000L)
}

private fun getVideoThumbnailNetwork(videoUri: Uri): Bitmap? {
    val retriever = FFmpegMediaMetadataRetriever()
    retriever.setDataSource(videoUri.toString())
    return retriever.getFrameAtTime(1000L, FFmpegMediaMetadataRetriever.OPTION_CLOSEST)
}