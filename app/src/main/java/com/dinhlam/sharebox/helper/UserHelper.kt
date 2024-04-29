package com.dinhlam.sharebox.helper

import android.content.Context
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHelper @Inject constructor(
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
) {
    fun isSignedIn(): Boolean {
        val currentUserId = userSharePref.getCurrentUserId()
        val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email ?: return false
        val firebaseUserId = UserUtils.createUserId(firebaseEmail)
        return currentUserId == firebaseUserId
    }

    fun getCurrentUserId(): String {
        if (!isSignedIn()) {
            return userSharePref.getAnonymousUserId()
        }
        return userSharePref.getCurrentUserId()
    }

    suspend fun createUser(
        userId: String,
        displayName: String,
        avatarUrl: String,
        onSuccess: suspend (User) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val existedUser = withContext(Dispatchers.IO) {
                userRepository.findOneRaw(userId)
                    ?.copy(name = displayName, avatar = avatarUrl)
            }

            val shareBoxUser = withContext(Dispatchers.IO) {
                existedUser?.let { user -> userRepository.update(user) } ?: userRepository.insert(
                    userId,
                    displayName,
                    avatarUrl
                )
            }

            shareBoxUser?.let { createdUser ->
                userSharePref.setCurrentUserId(createdUser.userId)
                onSuccess(createdUser)
            } ?: error("Create user error")
        } catch (e: Exception) {
            onError(e)
        }
    }

    suspend fun updateUserAvatar(userId: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        val shareBoxUser = userRepository.findOneRaw(userId) ?: return@withContext
        val newUser = shareBoxUser.copy(avatar = avatarUrl)
        userRepository.update(newUser)
    }

    fun signOut(context: Context, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        AuthUI.getInstance().signOut(context).addOnSuccessListener {
            userSharePref.clearCurrentUserId()
            onSuccess()
        }.addOnFailureListener(onError)
    }
}