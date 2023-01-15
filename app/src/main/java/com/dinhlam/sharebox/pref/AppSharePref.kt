package com.dinhlam.sharebox.pref

import android.content.Context
import androidx.core.content.edit
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.model.SortType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharePref @Inject constructor(
    @ApplicationContext context: Context
) : SharePref(context, "share_saver_pref") {

    companion object {
        private const val KEY_APP_FIRST_LAUNCH = "is-app-first-launch"
        private const val KEY_LAST_FOLDER_SELECTED = "last-folder-selected"

        private const val KEY_SORT_SELECTED_OPTION = "sort-selected-option"

        private const val KEY_SHOW_GUIDELINE = "show-guide-line"

        private const val KEY_PASSWORD_RECOVERY = "recovery-password"
    }

    fun isAppFirstLaunch(): Boolean = sharePref.getBoolean(KEY_APP_FIRST_LAUNCH, false)

    fun commitAppFirstLaunch() = sharePref.edit(true) { putBoolean(KEY_APP_FIRST_LAUNCH, true) }

    fun getLastSelectedFolder(): String = sharePref.getString(KEY_LAST_FOLDER_SELECTED, "") ?: ""

    fun setLastSelectedFolder(folderId: String) = put(KEY_LAST_FOLDER_SELECTED, folderId)

    fun setSortType(sortType: SortType) {
        sharePref.edit {
            putInt(
                KEY_SORT_SELECTED_OPTION, when (sortType) {
                    SortType.NEWEST -> 1
                    SortType.OLDEST -> 2
                    else -> 0
                }
            )
        }

    }

    fun getSortType(): SortType {
        return when (sharePref.getInt(KEY_SORT_SELECTED_OPTION, 0)) {
            1 -> SortType.NEWEST
            2 -> SortType.OLDEST
            else -> SortType.NONE
        }
    }

    fun isShowGuideLine(): Boolean {
        return BuildConfig.DEBUG || sharePref.getBoolean(KEY_SHOW_GUIDELINE, true)
    }

    fun turnOffShowGuideline() {
        sharePref.edit { putBoolean(KEY_SHOW_GUIDELINE, false) }
    }

    fun setRecoveryPassword(recoveryPasswordHash: String) =
        sharePref.edit { putString(KEY_PASSWORD_RECOVERY, recoveryPasswordHash) }

    fun getRecoveryPassword(): String =
        sharePref.getString(KEY_PASSWORD_RECOVERY, "") ?: ""
}
