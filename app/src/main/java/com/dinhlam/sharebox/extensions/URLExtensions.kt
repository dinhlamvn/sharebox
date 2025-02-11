package com.dinhlam.sharebox.extensions

import android.webkit.MimeTypeMap

fun String.isYoutubeVideo(): Boolean {
    return contains("youtube.com") || contains("youtu.be")
}

fun String.isTiktokVideo(): Boolean {
    return contains("tiktok.com")
}

fun String.isFacebookVideo(): Boolean {
    return (contains(Regex("facebook.com|fb.com|fb.watch")) && (contains("watch") || contains(
        "/videos/"
    ) || contains("reel") || contains("stories"))) || contains("fb.gg/v/")
}

fun String.isImageUrl() = arrayOf(
    ".png", ".jpg", ".jpeg", ".webp", ".gif"
).any { ext -> endsWith(ext, true) }

fun String.isImageMimeType() = arrayOf(
    "png", "jpg", "jpeg", "webp", "gif"
).any { ext -> endsWith(ext, true) }

fun String.isVideoUrl() = arrayOf(
    ".mp4", ".avi", ".webm", ".flv", ".mov", ".m4v", ".3gp"
).any { ext -> endsWith(ext, true) }

fun String.isVideoMimeType() = arrayOf(
    "mp4", "avi", "webm", "flv", "mov", "m4v", "3gp"
).any { ext -> endsWith(ext, true) }

fun String.isAudioUrl() = arrayOf(
    ".mp3", ".wav",
).any { ext -> endsWith(ext, true) }

fun String.isAudioMimeType() = arrayOf(
    "mp3", "wav",
).any { ext -> endsWith(ext, true) }

val String.ext: String?
    get() = MimeTypeMap.getFileExtensionFromUrl(this)

val String.mimeType: String?
    get() = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this))