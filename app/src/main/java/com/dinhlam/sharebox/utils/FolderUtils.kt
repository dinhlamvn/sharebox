package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.database.entity.Folder

object FolderUtils {

    private const val FOLDER_HOME_ID = "folder_home"
    private const val FOLDER_TEXT_ID = "folder_text"
    private const val FOLDER_WEB_ID = "folder_web"
    private const val FOLDER_IMAGE_ID = "folder_image"

    fun getDefaultFolders() = listOf(
        Folder(FOLDER_HOME_ID, "Home", "For all"),
        Folder(FOLDER_TEXT_ID, "Texts", "For plain text"),
        Folder(FOLDER_WEB_ID, "Webs", "For web link"),
        Folder(FOLDER_IMAGE_ID, "Images", "For image")
    )

    fun isProtectedFolder(id: String): Boolean {
        return id in arrayOf(FOLDER_HOME_ID, FOLDER_TEXT_ID, FOLDER_WEB_ID, FOLDER_IMAGE_ID)
    }
}