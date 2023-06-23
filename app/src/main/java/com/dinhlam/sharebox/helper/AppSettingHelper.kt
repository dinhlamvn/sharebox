package com.dinhlam.sharebox.helper

import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.pref.AppSharePref
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingHelper @Inject constructor(private val appSharePref: AppSharePref) {

    fun getTheme(): AppSettings.Theme {
        return when (appSharePref.getTheme()) {
            1 -> AppSettings.Theme.LIGHT
            2 -> AppSettings.Theme.DARK
            else -> AppSettings.Theme.AUTOMATIC
        }
    }

    fun setTheme(theme: AppSettings.Theme) {
        val num = when (theme) {
            AppSettings.Theme.LIGHT -> 1
            AppSettings.Theme.DARK -> 2
            else -> 0
        }
        appSharePref.setTheme(num)
    }

    fun getNetworkCondition(): AppSettings.NetworkCondition {
        return when (appSharePref.getNetworkCondition()) {
            1 -> AppSettings.NetworkCondition.WIFI_CELLULAR_DATA
            else -> AppSettings.NetworkCondition.WIFI_ONLY
        }
    }

    fun setNetworkCondition(networkCondition: AppSettings.NetworkCondition) {
        val num = when (networkCondition) {
            AppSettings.NetworkCondition.WIFI_CELLULAR_DATA -> 1
            else -> 0
        }
        appSharePref.setNetworkCondition(num)
    }
}