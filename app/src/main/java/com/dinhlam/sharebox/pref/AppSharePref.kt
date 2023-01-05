package com.dinhlam.sharebox.pref

import android.content.Context
import androidx.core.content.edit
import com.dinhlam.sharebox.model.SortType
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

        private const val SORT_SELECTED_OPTION = "sort-selected-option"
    }

    fun isAppFirstLaunch(): Boolean = sharePref.getBoolean(PREF_APP_FIRST_LAUNCH, false)

    fun commitAppFirstLaunch() = sharePref.edit(true) { putBoolean(PREF_APP_FIRST_LAUNCH, true) }

    fun getLastSelectedFolder(): String = sharePref.getString(PREF_LAST_FOLDER_SELECTED, "") ?: ""

    fun setLastSelectedFolder(folderId: String) = put(PREF_LAST_FOLDER_SELECTED, folderId)

    fun setSortType(sortType: SortType) {
        sharePref.edit {
            putInt(
                SORT_SELECTED_OPTION, when (sortType) {
                    SortType.NEWEST -> 1
                    SortType.OLDEST -> 2
                    else -> 0
                }
            )
        }

    }

    fun getSortType(): SortType {
        return when (sharePref.getInt(SORT_SELECTED_OPTION, 0)) {
            1 -> SortType.NEWEST
            2 -> SortType.OLDEST
            else -> SortType.NONE
        }
    }
}
