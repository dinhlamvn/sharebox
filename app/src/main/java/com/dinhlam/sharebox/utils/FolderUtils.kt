package com.dinhlam.sharebox.utils

object FolderUtils {

    const val FOLDER_HOME_ID = "folder_home"
    const val FOLDER_TEXT_ID = "folder_text"
    const val FOLDER_WEB_ID = "folder_web"
    const val FOLDER_IMAGE_ID = "folder_image"

    fun isProtectedFolder(id: String): Boolean {
        return id in arrayOf(FOLDER_HOME_ID, FOLDER_TEXT_ID, FOLDER_WEB_ID, FOLDER_IMAGE_ID)
    }
}