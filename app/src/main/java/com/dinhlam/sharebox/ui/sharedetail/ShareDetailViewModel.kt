package com.dinhlam.sharebox.ui.sharedetail

import androidx.annotation.UiThread
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.R
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
        execute {
            val comments = commentRepository.find(share.shareId)
            copy(comments = comments)
        }
    }

    private fun loadShareDetail() = execute {
        val share = shareRepository.findOne(shareId)
        copy(share = share)
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

    fun getCurrentUserProfile() = doInBackground {
        val user = userRepository.findOne(userHelper.getCurrentUserId())
            ?: return@doInBackground setState { copy(currentUser = null) }
        setState { copy(currentUser = user) }
    }
}