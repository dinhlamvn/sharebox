package com.dinhlam.sharebox.helper

import android.content.Context
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHelper @Inject constructor(
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository
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
        avatarUrl: String
    ): User? {
        val existedUser = userRepository.findOneRaw(userId)
            ?.copy(name = displayName, avatar = avatarUrl)

        val shareBoxUser = withContext(Dispatchers.IO) {
            existedUser?.let { user -> userRepository.update(user) } ?: userRepository.insert(
                userId,
                displayName,
                avatarUrl
            )
        }

        return shareBoxUser?.also { createdUser -> userSharePref.setCurrentUserId(createdUser.userId) }
    }

    suspend fun updateUserAvatar(userId: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        val shareBoxUser = userRepository.findOneRaw(userId) ?: return@withContext
        val newUser = shareBoxUser.copy(avatar = avatarUrl)
        userRepository.update(newUser)
    }

    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onSuccess: suspend () -> Unit,
        onError: suspend (Throwable) -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                AuthUI.getInstance().signOut(context).await()
                userSharePref.clearCurrentUserId()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }

        }
    }
}