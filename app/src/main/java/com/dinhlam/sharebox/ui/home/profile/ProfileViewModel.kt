package com.dinhlam.sharebox.ui.home.profile

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
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
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val boxRepository: BoxRepository,
) : BaseViewModel<ProfileState>(ProfileState()) {
    init {
        getCurrentUserProfile()
        consume(ProfileState::currentBox) {
            loadShares()
            loadBoxes()
        }
    }

    private fun getCurrentUserProfile() = doInBackground {
        val user = userRepository.findOne(userHelper.getCurrentUserId()) ?: return@doInBackground
        setState { copy(currentUser = user) }
    }

    private fun loadShares() {
        execute {
            val shares = loadShares(currentBox, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
            copy(shares = shares, isRefreshing = false)
        }
    }

    private fun loadBoxes() {
        if (!userHelper.isSignedIn()) {
            return
        }
        doInBackground {
            val boxes = boxRepository.findByUser(
                userHelper.getCurrentUserId(),
                100,
                0
            )
            setState { copy(boxes = boxes) }
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val others = loadShares(
                currentBox,
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

    private suspend fun loadShares(
        boxDetail: BoxDetail?,
        limit: Int,
        offset: Int
    ): List<ShareDetail> {
        return boxDetail?.let { box ->
            shareRepository.findWhereInBox(
                userHelper.getCurrentUserId(),
                box.boxId,
                limit,
                offset
            )
        } ?: shareRepository.find(userHelper.getCurrentUserId(), limit, offset)
    }

    fun doOnRefresh() = getState { state ->
        if (state.currentBox == null) {
            setState { ProfileState() }
            getCurrentUserProfile()
            loadShares()
            loadBoxes()
        } else {
            setState { ProfileState() }
            getCurrentUserProfile()
        }
    }

    fun like(shareId: String) = doInBackground {
        val result =
            likeRepository.like(shareId, userHelper.getCurrentUserId()) ?: return@doInBackground
        realtimeDatabaseRepository.push(result)
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

    fun setBox(box: BoxDetail?) = getState { state ->
        if (state.currentBox != box) {
            setState { copy(currentBox = box) }
        } else {
            setState { copy(currentBox = null) }
        }
    }
}