package com.dinhlam.sharebox.ui.sharedetail

import androidx.annotation.UiThread
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.utils.CommentUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShareDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val shareRepository: ShareRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
    private val videoHelper: VideoHelper,
) : BaseViewModel<ShareDetailState>(ShareDetailState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID))) {

    init {
        getCurrentUserProfile()
        loadShareDetail()

        consume(ShareDetailState::share) { shareDetail ->
            loadComments(shareDetail)
        }
    }

    private fun loadComments(shareDetail: ShareDetail?) {
        val share = shareDetail ?: return setState { copy(comments = emptyList()) }
        suspend { commentRepository.find(share.shareId) }.execute { comments ->
            copy(comments = comments)
        }
    }

    private fun loadShareDetail() = getState { state ->
        suspend { shareRepository.findOne(state.shareId) }.execute { shareDetail ->
            copy(share = shareDetail)
        }
    }

    fun onRefresh() {
        setState { ShareDetailState(savedStateHandle.getNonNull(AppExtras.EXTRA_SHARE_ID)) }
        getCurrentUserProfile()
        loadShareDetail()
    }

    fun like(shareId: String) = doInBackground {
        val result =
            likeRepository.like(shareId, userHelper.getCurrentUserId()) ?: return@doInBackground
        realtimeDatabaseRepository.push(result)
        setState {
            val likeNumber = share?.likeNumber ?: 0
            copy(share = share?.copy(likeNumber = likeNumber + 1, liked = true))
        }
    }

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun sendComment(s: String) = getState { state ->
        suspend {
            val comment = commentRepository.insert(
                CommentUtils.createCommentId(), state.shareId, userHelper.getCurrentUserId(), s
            )
            comment?.let { cmt ->
                realtimeDatabaseRepository.push(cmt)
                state.comments.plus(commentRepository.findOne(cmt.commentId)!!)
            } ?: state.comments
        }.execute { comments ->
            copy(comments = comments)
        }
    }

    fun getCurrentUserProfile() = doInBackground {
        val user = userRepository.findOne(userHelper.getCurrentUserId())
            ?: return@doInBackground setState { copy(currentUser = null) }
        setState { copy(currentUser = user) }
    }
}