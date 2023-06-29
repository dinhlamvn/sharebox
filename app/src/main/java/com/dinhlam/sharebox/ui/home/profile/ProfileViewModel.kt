package com.dinhlam.sharebox.ui.home.profile

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val likeRepository: LikeRepository,
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        getCurrentUserProfile()
        loadShares()
    }

    private fun getCurrentUserProfile() = doInBackground {
        val user = userRepository.findOne(userHelper.getCurrentUserId()) ?: return@doInBackground
        setState { copy(currentUser = user) }
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        execute {
            val shares = shareRepository.find(
                userHelper.getCurrentUserId(), AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0
            )
            copy(shares = shares, isRefreshing = false)
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val others = shareRepository.find(
                userHelper.getCurrentUserId(),
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
            copy(
                shares = shares.plus(others),
                isLoadingMore = false,
                currentPage = currentPage + 1,
                canLoadMore = others.isNotEmpty()
            )
        }
    }

    fun doOnRefresh() {
        setState { ProfileState() }
        getCurrentUserProfile()
        loadShares()
    }

    fun like(shareId: String) = doInBackground {
        val result = likeRepository.likeAndSyncToCloud(shareId, userHelper.getCurrentUserId())
        if (result) {
            setState {
                val shareList = shares.map { shareDetail ->
                    if (shareDetail.shareId == shareId) {
                        shareDetail.copy(likeNumber = shareDetail.likeNumber + 1, liked = true)
                    } else {
                        shareDetail
                    }
                }
                copy(shares = shareList)
            }
        }
    }

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun bookmark(shareId: String, bookmarkCollectionId: String?) = doInBackground {
        bookmarkCollectionId?.let { id ->
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            if (bookmarkDetail?.bookmarkCollectionId != bookmarkCollectionId) {
                val bookmarked =
                    bookmarkRepository.bookmark(bookmarkDetail?.id.orElse(0), shareId, id)
                if (bookmarked) {
                    setState {
                        val shareList = shares.map { shareDetail ->
                            if (shareDetail.shareId == shareId) {
                                shareDetail.copy(bookmarked = true)
                            } else {
                                shareDetail
                            }
                        }
                        copy(shares = shareList)
                    }
                }
            }
        } ?: run {
            val deleted = bookmarkRepository.delete(shareId)
            if (deleted) {
                setState {
                    val shareList = shares.map { shareDetail ->
                        if (shareDetail.shareId == shareId) {
                            shareDetail.copy(bookmarked = false)
                        } else {
                            shareDetail
                        }
                    }
                    copy(shares = shareList)
                }
            }
        }
    }
}