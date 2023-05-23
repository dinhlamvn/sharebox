package com.dinhlam.sharebox.helper

import android.net.Uri
import com.dinhlam.sharebox.data.model.VideoSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoHelper @Inject constructor() {

    fun getVideoSource(url: String): VideoSource {
        return when {
            isYoutubeVideo(url) -> VideoSource.Youtube
            isTiktokVideo(url) -> VideoSource.Tiktok
            isFacebookVideo(url) -> VideoSource.Facebook
            else -> error("No source found for url $url")
        }
    }

    fun getVideoSourceId(url: String): String {
        return when {
            isYoutubeVideo(url) -> getYoutubeVideoSourceId(url)
            isTiktokVideo(url) -> getTiktokVideoSourceId(url)
            isFacebookVideo(url) -> getFacebookVideoSourceId(url)
            else -> error("No source id found for url $url")
        }
    }

    private fun getYoutubeVideoSourceId(url: String): String {
        val uri = Uri.parse(url)
        return if (url.contains("/shorts/") || url.contains("youtu.be")) {
            uri.lastPathSegment!!
        } else {
            uri.getQueryParameter("v")!!
        }
    }

    private fun getTiktokVideoSourceId(url: String): String {
        return ""
    }

    private fun getFacebookVideoSourceId(url: String): String {
        return ""
    }

    private fun isYoutubeVideo(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun isTiktokVideo(url: String): Boolean {
        return url.contains("tiktok.com")
    }

    private fun isFacebookVideo(url: String): Boolean {
        return url.contains("tiktok.com")
    }
}