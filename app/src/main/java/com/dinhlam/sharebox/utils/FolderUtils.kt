package com.dinhlam.sharebox.utils

import android.content.Context
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.database.entity.Folder

object FolderUtils {

    private const val FOLDER_HOME_ID = "folder_home"

    fun getDefaultFolders(context: Context) = listOf(
        Folder(
            FOLDER_HOME_ID,
            context.getString(R.string.default_folder_name),
            context.getString(R.string.default_folder_desc)
        ),
        Folder("tiktok_video", "Tiktok", "Video from Tiktok"),
        Folder("youtube_short", "Youtube Shorts", "Video from Youtube Shorts"),
        Folder("facebook_reels", "Facebook Reels", "Video from Facebook Reels"),
    )

    fun isProtectedFolder(id: String): Boolean {
        return id in arrayOf(FOLDER_HOME_ID)
    }
}