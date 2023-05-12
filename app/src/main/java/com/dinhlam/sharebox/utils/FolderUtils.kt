package com.dinhlam.sharebox.utils

import android.content.Context
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.model.ShareData

object FolderUtils {

    const val FOLDER_HOME_ID = "folder_home"

    private const val TIKTOK = "tiktok_video"
    private const val YOUTUBE_SHORTS = "youtube_shorts"
    private const val FACEBOOK_REELS = "facebook_reels"

    fun getDefaultFolders(context: Context) = listOf(
        Folder(
            FOLDER_HOME_ID,
            context.getString(R.string.default_folder_name),
            context.getString(R.string.default_folder_desc)
        ),
        Folder(TIKTOK, "Tiktok", "Video from Tiktok"),
        Folder(YOUTUBE_SHORTS, "Youtube Shorts", "Video from Youtube Shorts"),
        Folder(FACEBOOK_REELS, "Facebook Reels", "Video from Facebook Reels"),
    )

    fun isProtectedFolder(id: String): Boolean {
        return id in arrayOf(FOLDER_HOME_ID)
    }

    fun isDefaultFolder(id: String): Boolean {
        return id in arrayOf(YOUTUBE_SHORTS, FACEBOOK_REELS, TIKTOK)
    }

    fun getFolderIdByShareContent(shareInfo: ShareData): String? {
        return when (shareInfo) {
            is ShareData.ShareUrl -> {
                val url = shareInfo.url ?: return FOLDER_HOME_ID
                if (url.contains("youtube.com/shorts")) {
                    YOUTUBE_SHORTS
                } else if (url.contains("facebook.com/reel")) {
                    FACEBOOK_REELS
                } else if (url.contains("tiktok")) {
                    TIKTOK
                } else null
            }
            else -> null
        }
    }
}