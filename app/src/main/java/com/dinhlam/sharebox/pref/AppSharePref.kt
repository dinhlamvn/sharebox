package com.dinhlam.sharebox.pref

import android.content.Context
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

        private const val KEY_CUSTOM_TAB_ENABLED = "custom-tab-enabled"

        private const val KEY_SHOW_RECENTLY_SHARE = "show-recently-share"
    }

    fun isAppFirstLaunch(): Boolean = get(KEY_APP_FIRST_LAUNCH, Boolean::class.java, true)

    fun commitAppFirstLaunch() = put(KEY_APP_FIRST_LAUNCH, value = false, sync = true)

    fun getLastSelectedFolder(): String = get(KEY_LAST_FOLDER_SELECTED, String::class.java, "")

    fun setLastSelectedFolder(folderId: String) = put(KEY_LAST_FOLDER_SELECTED, folderId)

    fun setSortType(sortType: SortType) {
        put(
            KEY_SORT_SELECTED_OPTION, when (sortType) {
                SortType.NEWEST -> 1
                SortType.OLDEST -> 2
                else -> 0
            }
        )
    }

    fun getSortType(): SortType {
        return when (get(KEY_SORT_SELECTED_OPTION, Int::class.java, 0)) {
            1 -> SortType.NEWEST
            2 -> SortType.OLDEST
            else -> SortType.NONE
        }
    }

    fun isShowGuideLine(): Boolean {
        return BuildConfig.DEBUG || get(KEY_SHOW_GUIDELINE, Boolean::class.java, true)
    }

    fun turnOffShowGuideline() {
        put(KEY_SHOW_GUIDELINE, false)
    }

    fun setRecoveryPassword(recoveryPasswordHash: String) =
        put(KEY_PASSWORD_RECOVERY, recoveryPasswordHash)

    fun getRecoveryPassword(): String = get(KEY_PASSWORD_RECOVERY, String::class.java, "")

    fun isCustomTabEnabled() = get(KEY_CUSTOM_TAB_ENABLED, Boolean::class.java, true)

    fun toggleCustomTab(isOn: Boolean) = put(KEY_CUSTOM_TAB_ENABLED, isOn)

    fun isShowRecentlyShare() = get(KEY_SHOW_RECENTLY_SHARE, Boolean::class.java, true)

    fun toggleShowRecentlyShare(showRecentlyShare: Boolean) =
        put(KEY_SHOW_RECENTLY_SHARE, showRecentlyShare)
}
