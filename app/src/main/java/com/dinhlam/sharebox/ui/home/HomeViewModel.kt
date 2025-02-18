package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.TagRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
    private val boxRepository: BoxRepository,
    private val appSettingHelper: AppSettingHelper,
    private val tagRepository: TagRepository,
) : BaseViewModel<HomeState>(HomeState(userHelper.getCurrentUserId())) {

    companion object {
        private const val BOX_LIST_INIT_LOAD_SIZE_DEFAULT = 3
    }

    fun refresh() {
        getTotalBox()
        getListBoxes()
        getRecentlyShares()
    }

    private fun getTotalBox() = suspend {
        boxRepository.count()
    }.execute { asyncLoad ->
        copy(totalBox = asyncLoad.data ?: 0)
    }

    private fun getListBoxes() = suspend {
        boxRepository.find(BOX_LIST_INIT_LOAD_SIZE_DEFAULT, 0)
    }.execute { asyncLoad ->
        copy(boxes = asyncLoad.completed.ifTrue(asyncLoad.data.orEmpty(), boxes))
    }

    private fun getRecentlyShares() {
        suspend {
            shareRepository.findRecentlyShares(
                userHelper.getCurrentUserId(),
                appSettingHelper.getNumOfRecently(),
                0
            )
        }.execute { asyncLoad ->
            val list = asyncLoad.data.orEmpty()
            copy(
                shares = asyncLoad.completed.ifTrue(list, shares),
                isRefreshing = asyncLoad is AsyncLoad.Loading
            )
        }
    }

    fun like(shareId: String) = setState {
        val shareList = shares.map { shareDetail ->
            if (shareDetail.shareId == shareId) {
                shareDetail.copy(likeNumber = shareDetail.likeNumber + 1, liked = true)
            } else {
                shareDetail
            }
        }
        copy(shares = shareList)
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

    fun setChooseBoxFor(chooseBoxFor: HomeState.ChooseBoxFor?) = setState {
        copy(chooseBoxFor = chooseBoxFor)
    }

    fun refreshBoxDetail(boxDetail: BoxDetail) = getState { state ->
        val boxes = state.boxes.map { box ->
            if (box.boxId == boxDetail.boxId) {
                boxDetail
            } else {
                box
            }
        }
        setState { copy(boxes = boxes) }
    }

    fun reloadBoxDetail(id: String) {
        suspend {
            boxRepository.findOne(id)!!
        }.execute { asyncLoad ->
            if (asyncLoad is AsyncLoad.Success) {
                val box = asyncLoad.value
                val newBoxList = boxes.map { boxDetail ->
                    if (boxDetail.boxId == id) {
                        box
                    } else {
                        boxDetail
                    }
                }
                copy(boxes = newBoxList)
            } else {
                this
            }
        }
    }
}