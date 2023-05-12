package com.dinhlam.sharebox.pref

import android.content.Context
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.utils.ShareUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSharePref @Inject constructor(
    @ApplicationContext context: Context
) : SharePref(context, "share-box-user-pref") {

    companion object {
        private const val KEY_ACTIVE_USER_ID = "active-user-id"
        private const val KEY_ACTIVE_SHARE_MODE = "active-share-mode"
    }

    fun getActiveUserId() = get(KEY_ACTIVE_USER_ID, "")

    fun setActiveUserId(userId: String) = put(KEY_ACTIVE_USER_ID, userId, true)

    fun getActiveShareMode(): ShareMode {
        return when (get(KEY_ACTIVE_SHARE_MODE, ShareUtils.SHARE_MODE_COMMUNITY)) {
            ShareUtils.SHARE_MODE_PERSONAL -> ShareMode.ShareModePersonal
            else -> ShareMode.ShareModeCommunity
        }
    }

    fun setActiveShareMode(shareMode: ShareMode) = put(KEY_ACTIVE_SHARE_MODE, shareMode.mode, true)
}