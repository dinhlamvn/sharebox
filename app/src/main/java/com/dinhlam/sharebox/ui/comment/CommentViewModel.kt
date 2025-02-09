package com.dinhlam.sharebox.ui.comment

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
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

    fun getCurrentUserProfile() {
        suspend { userRepository.findOne(userHelper.getCurrentUserId()) }
            .execute { asyncLoad ->
                copy(currentUser = asyncLoad.data)
            }
    }

    private fun loadComments() = getState { state ->
        suspend {
            commentRepository.find(state.shareId)
        }.execute { asyncLoad ->
            copy(comments = asyncLoad.data.orEmpty(), isRefreshing = asyncLoad is AsyncLoad.Loading)
        }
    }

    fun sendComment(comment: String) = getState { state ->
        suspend {
            val cmtEntity = commentRepository.insert(
                CommentUtils.createCommentId(),
                state.shareId,
                userHelper.getCurrentUserId(),
                comment
            )!!
            commentRepository.findOne(cmtEntity.commentId)
        }.execute { asyncLoad ->
            asyncLoad.data?.let { cmtEntity ->
                val newList = comments.toMutableList()
                newList.add(0, cmtEntity)
                copy(comments = newList)
            } ?: this
        }
    }
}