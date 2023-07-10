package com.dinhlam.sharebox.helper

import android.app.Application
import android.content.Context
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
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
    private val application: Application,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val shareRepository: ShareRepository,
) {

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
            } ?: throw CreateUserError
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

    suspend fun syncUserInfo(): User? = withContext(Dispatchers.IO) {
        if (!isSignedIn()) {
            return@withContext null
        }

        val currentUserId = getCurrentUserId()
        val user = userRepository.findOneRaw(currentUserId) ?: return@withContext null

        val commentCount = commentRepository.countByUser(currentUserId)
        val likeCount = likeRepository.countByUserShare(currentUserId)
        val shareCount = shareRepository.countByUser(currentUserId)

        val drama = commentCount + likeCount * 10 + shareCount * 10
        val level = getLevelByDrama(drama)

        val newUser = user.copy(drama = drama, level = level)
        userRepository.update(newUser)
    }

    private fun getLevelByDrama(drama: Int): Int {
        return when (drama) {
            in 0..1000 -> 0
            in 1001..3000 -> 1
            in 3001..10000 -> 2
            else -> 3
        }
    }
}