package com.dinhlam.sharebox.pref

import android.content.Context
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.utils.UserUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSharePref @Inject constructor(@ApplicationContext context: Context) :
    SharePref(context, "share-box-user-pref") {

    companion object {
        private const val KEY_CURRENT_USER_ID = "current-user-id"
    }

    fun getCurrentUserId() = if (BuildConfig.DEBUG && AppConsts.IS_FAKE_USER_ID) {
        UserUtils.createUserId("dinh.lam.jx2@gmail.com")
    } else get(KEY_CURRENT_USER_ID, "")

    fun setCurrentUserId(userId: String) = put(KEY_CURRENT_USER_ID, userId, true)

    fun clearCurrentUserId() = remove(KEY_CURRENT_USER_ID, true)
}