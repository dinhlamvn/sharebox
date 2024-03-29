package com.dinhlam.sharebox.pref

import android.content.Context
import androidx.annotation.IntRange
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharePref @Inject constructor(
    @ApplicationContext context: Context
) : SharePref(context, "share_saver_pref") {

    companion object {
        private const val KEY_CUSTOM_TAB_ENABLED = "setting-custom-tab-enabled"
        private const val KEY_SETTING_MODE_THEME = "setting-theme"
        private const val KEY_SETTING_NETWORK_CONDITION = "setting-network-condition"
        private const val KEY_LATEST_ACTIVE_BOX_ID = "lasted-active-box-id"
        private const val KEY_SHOW_GUIDELINE = "show-guideline"
        private const val KEY_SETTING_IMAGE_DOWNLOAD_QUALITY = "setting-download-image-quality"
        private const val KEY_SETTING_SYNC_IN_BACKGROUND = "setting-sync-in-background"
        private const val KEY_FIRST_INSTALL = "key-first-install"
    }

    fun setTheme(@IntRange(from = 0, to = 2) theme: Int) {
        put(KEY_SETTING_MODE_THEME, theme)
    }

    fun setNetworkCondition(@IntRange(from = 0, to = 1) networkCondition: Int) {
        put(KEY_SETTING_NETWORK_CONDITION, networkCondition)
    }

    @IntRange(from = 0, to = 2)
    fun getTheme(): Int {
        return get(KEY_SETTING_MODE_THEME, 0)
    }

    @IntRange(from = 0, to = 1)
    fun getNetworkCondition(): Int {
        return get(KEY_SETTING_NETWORK_CONDITION, 1)
    }

    fun isCustomTabEnabled() = get(KEY_CUSTOM_TAB_ENABLED, true)

    fun getLatestActiveBoxId() = get(KEY_LATEST_ACTIVE_BOX_ID, "")

    fun setLatestActiveBoxId(boxId: String) = put(KEY_LATEST_ACTIVE_BOX_ID, boxId)

    fun isShowGuideline(): Boolean = get(KEY_SHOW_GUIDELINE, true)

    fun offShowGuideline() {
        put(KEY_SHOW_GUIDELINE, value = false, sync = true)
    }

    fun setImageDownloadQuality(quality: Int) {
        put(KEY_SETTING_IMAGE_DOWNLOAD_QUALITY, quality)
    }

    fun getImageDownloadQuality(): Int {
        return get(KEY_SETTING_IMAGE_DOWNLOAD_QUALITY, 80)
    }

    fun setSyncInBackground(syncInBackground: Boolean) =
        put(KEY_SETTING_SYNC_IN_BACKGROUND, syncInBackground, true)

    fun isSyncInBackground() = get(KEY_SETTING_SYNC_IN_BACKGROUND, false)

    fun isFirstInstall() = get(KEY_FIRST_INSTALL, true)

    fun offFirstInstall() = put(KEY_FIRST_INSTALL, value = false, sync = true)
}
