package com.dinhlam.sharebox.ui.comment

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.CommentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CommentState>(CommentState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID))) {

    init {
        getActiveUserInfo()
        loadComments()
    }

    private fun getActiveUserInfo() = backgroundTask {
        val activeUserId =
            userSharePref.getActiveUserId().takeIfNotNullOrBlank() ?: return@backgroundTask
        val user = userRepository.findOne(activeUserId) ?: return@backgroundTask
        setState { copy(activeUser = user) }
    }

    private fun loadComments() = execute { state ->
        setState { copy(isRefreshing = true) }
        val comments = commentRepository.find(state.shareId)
        setState { copy(comments = comments, isRefreshing = false) }
    }

    private fun reloadComments() = execute { state ->
        val comments = commentRepository.find(state.shareId)
        setState { copy(comments = comments) }
    }

    fun sendComment(comment: String) = execute { state ->
        val commentEntity = commentRepository.insert(
            CommentUtils.createCommentId(), state.shareId, userSharePref.getActiveUserId(), comment
        )

        commentEntity?.let { cmtEntity ->
            realtimeDatabaseRepository.push(cmtEntity)
            reloadComments()
        } ?: postShowToast(R.string.error_send_comment)
    }
}