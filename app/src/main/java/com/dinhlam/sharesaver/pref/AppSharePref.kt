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
    }

    fun isAppFirstLaunch(): Boolean = sharePref.getBoolean(PREF_APP_FIRST_LAUNCH, false)

    fun commitAppFirstLaunch() = sharePref.edit(true) { putBoolean(PREF_APP_FIRST_LAUNCH, true) }
}