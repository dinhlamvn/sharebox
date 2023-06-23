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
        return get(KEY_SETTING_NETWORK_CONDITION, 0)
    }
    fun isCustomTabEnabled() = get(KEY_CUSTOM_TAB_ENABLED, true)
}
