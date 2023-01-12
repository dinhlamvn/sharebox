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
        )
    )

    fun isProtectedFolder(id: String): Boolean {
        return id in arrayOf(FOLDER_HOME_ID)
    }
}