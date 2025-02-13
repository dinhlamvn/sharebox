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
        private const val KEY_SETTING_MODE_THEME = "setting-theme"
        private const val KEY_SETTING_NETWORK_CONDITION = "setting-network-condition"
        private const val KEY_SETTING_IMAGE_DOWNLOAD_QUALITY = "setting-download-image-quality"
        private const val KEY_SETTING_SYNC_IN_BACKGROUND = "setting-sync-in-background"
        private const val KEY_SETTING_NUM_OF_RECENTLY = "setting-num-of-recently"
        private const val KEY_FIRST_INSTALL = "app-first-install"
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

    fun setImageDownloadQuality(quality: Int) {
        put(KEY_SETTING_IMAGE_DOWNLOAD_QUALITY, quality)
    }

    fun getImageDownloadQuality(): Int {
        return get(KEY_SETTING_IMAGE_DOWNLOAD_QUALITY, 80)
    }

    fun setSyncInBackground(syncInBackground: Boolean) =
        put(KEY_SETTING_SYNC_IN_BACKGROUND, syncInBackground, true)

    fun isSyncInBackground() = get(KEY_SETTING_SYNC_IN_BACKGROUND, false)

    fun setNumOfRecently(quality: Int) {
        put(KEY_SETTING_NUM_OF_RECENTLY, quality)
    }

    fun getNumOfRecently(): Int {
        return get(KEY_SETTING_NUM_OF_RECENTLY, 10)
    }

    fun setAppFirstInstall(isFirstInstall: Boolean) {
        put(KEY_FIRST_INSTALL, isFirstInstall)
    }

    fun isAppFirstInstall(): Boolean {
        return get(KEY_FIRST_INSTALL, true)
    }
}
