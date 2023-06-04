package com.dinhlam.sharebox.helper

import android.content.Context
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.pref.SharePref
import com.dinhlam.sharebox.utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHelper @Inject constructor(
    @ApplicationContext context: Context,
    private val userRepository: UserRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) {

    class UserSharePref constructor(context: Context) : SharePref(context, "share-box-user-pref") {

        companion object {
            private const val KEY_CURRENT_USER_ID = "current-user-id"
        }

        fun getCurrentUserId() = get(KEY_CURRENT_USER_ID, "")

        fun setCurrentUserId(userId: String) = put(KEY_CURRENT_USER_ID, userId, true)

        fun clearCurrentUserId() = remove(KEY_CURRENT_USER_ID, true)
    }

    private val userSharePref by lazyOf(UserSharePref(context))

    object CreateUserError : Exception()

    fun isSignedIn(): Boolean {
        val currentUserId = userSharePref.getCurrentUserId()
        val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email ?: return false
        val firebaseUserId = UserUtils.createUserId(firebaseEmail)
        return currentUserId == firebaseUserId
    }

    fun getCurrentUserId(): String {
        if (!isSignedIn()) {
            return ""
        }
        return userSharePref.getCurrentUserId()
    }

    suspend fun createUser(
        email: String,
        displayName: String,
        avatarUrl: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val shareBoxUser = withContext(Dispatchers.IO) {
                userRepository.findOneRaw(UserUtils.createUserId(email)) ?: userRepository.insert(
                    email, displayName, avatarUrl
                )
            }

            shareBoxUser?.let { createdUser ->
                userSharePref.setCurrentUserId(createdUser.userId)
                realtimeDatabaseRepository.push(createdUser)
                onSuccess()
            } ?: throw CreateUserError
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun signOut(context: Context, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        AuthUI.getInstance().signOut(context).addOnSuccessListener {
            userSharePref.clearCurrentUserId()
            onSuccess()
        }.addOnFailureListener(onError)
    }
}