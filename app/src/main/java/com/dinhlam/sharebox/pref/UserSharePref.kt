package com.dinhlam.sharebox.pref

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSharePref @Inject constructor(
    @ApplicationContext context: Context
) : SharePref(context, "share-box-user-pref") {

    companion object {
        private const val KEY_ACTIVE_USER_ID = "active-user-id"
    }

    fun getActiveUserId() = get(KEY_ACTIVE_USER_ID, "")

    fun setActiveUserId(userId: String) = put(KEY_ACTIVE_USER_ID, userId, true)
}