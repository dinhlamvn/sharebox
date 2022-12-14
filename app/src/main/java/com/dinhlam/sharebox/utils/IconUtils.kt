package com.dinhlam.sharebox.utils

object IconUtils {
    private const val FB_ICON_URL = "https://img.icons8.com/fluency/144/000000/facebook-new.png"

    private const val INSTAGRAM_ICON_URL =
        "https://img.icons8.com/color/144/000000/instagram-new--v1.png"

    private const val TIKTOK_ICON_URL =
        "https://img.icons8.com/ios-filled/100/000000/tiktok--v1.png"

    private const val YOUTUBE_ICON_URL =
        "https://img.icons8.com/color/144/000000/youtube-play.png"

    fun getIconUrl(shareContent: String?): String? {
        if (isFacebookShare(shareContent)) {
            return FB_ICON_URL
        }
        if (isInstagramShare(shareContent)) {
            return INSTAGRAM_ICON_URL
        }
        if (isYoutubeShare(shareContent)) {
            return YOUTUBE_ICON_URL
        }
        if (isTiktokShare(shareContent)) {
            return TIKTOK_ICON_URL
        }
        return null
    }

    private fun isFacebookShare(content: String?): Boolean {
        val nonNull = content ?: return false
        return nonNull.contains("facebook.com") || nonNull.contains("https://fb")
    }

    private fun isInstagramShare(content: String?): Boolean {
        val nonNull = content ?: return false
        return nonNull.contains("instagram.com")
    }

    private fun isYoutubeShare(content: String?): Boolean {
        val nonNull = content ?: return false
        return nonNull.contains("youtu.be") || nonNull.contains("youtube.com")
    }

    private fun isTiktokShare(content: String?): Boolean {
        val nonNull = content ?: return false
        return nonNull.contains("tiktok.com")
    }
}
