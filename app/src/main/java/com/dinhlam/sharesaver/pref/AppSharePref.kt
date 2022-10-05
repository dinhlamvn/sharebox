package com.dinhlam.sharesaver.pref

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharePref @Inject constructor(
    @ApplicationContext context: Context
) : SharePref(context, "share_saver_pref") {

    companion object {
        private const val PREF_APP_FIRST_LAUNCH = "is-app-first-launch"
        private const val PREF_LAST_FOLDER_SELECTED = "last-folder-selected"
    }

    fun isAppFirstLaunch(): Boolean = sharePref.getBoolean(PREF_APP_FIRST_LAUNCH, false)

    fun commitAppFirstLaunch() = sharePref.edit(true) { putBoolean(PREF_APP_FIRST_LAUNCH, true) }

    fun getLastSelectedFolder(): String = sharePref.getString(PREF_LAST_FOLDER_SELECTED, "") ?: ""

    fun setLastSelectedFolder(folderId: String) = put(PREF_LAST_FOLDER_SELECTED, folderId)
}