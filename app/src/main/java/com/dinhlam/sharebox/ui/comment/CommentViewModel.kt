package com.dinhlam.sharebox.ui.comment

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.utils.CommentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CommentState>(CommentState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID))) {

    init {
        getCurrentUserProfile()
        loadComments()
    }

    fun getCurrentUserProfile() = doInBackground {
        val user = userRepository.findOne(userHelper.getCurrentUserId()) ?: return@doInBackground
        setState { copy(currentUser = user) }
    }

    private fun loadComments() {
        setState { copy(isRefreshing = true) }
        execute {
            val comments = commentRepository.find(shareId)
            copy(comments = comments, isRefreshing = false)
        }
    }

    fun sendComment(comment: String) = execute {
        val commentEntity = commentRepository.insert(
            CommentUtils.createCommentId(), shareId, userHelper.getCurrentUserId(), comment
        )

        commentEntity?.let { cmtEntity ->
            realtimeDatabaseRepository.push(cmtEntity)
            val newComment = commentRepository.findOne(cmtEntity.commentId) ?: return@execute copy()
            val newList = comments.toMutableList()
            newList.add(0, newComment)
            copy(comments = newList)
        } ?: run {
            postShowToast(R.string.error_send_comment)
            this
        }
    }
}