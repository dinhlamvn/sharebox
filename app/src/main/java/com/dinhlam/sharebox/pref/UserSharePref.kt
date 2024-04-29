package com.dinhlam.sharebox.pref

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSharePref @Inject constructor(@ApplicationContext context: Context) :
    SharePref(context, "share-box-user-pref") {

    companion object {
        private const val KEY_CURRENT_USER_ID = "current-user-id"
        private const val KEY_DEFAULT_USER_ID = "anonymous-user-id"
    }

    fun getAnonymousUserId() = get(KEY_DEFAULT_USER_ID, "")

    fun setAnonymousUserId(userId: String) = put(KEY_DEFAULT_USER_ID, userId, true)

    fun getCurrentUserId() = get(KEY_CURRENT_USER_ID, "")

    fun setCurrentUserId(userId: String) = put(KEY_CURRENT_USER_ID, userId, true)

    fun clearCurrentUserId() = remove(KEY_CURRENT_USER_ID, true)
}