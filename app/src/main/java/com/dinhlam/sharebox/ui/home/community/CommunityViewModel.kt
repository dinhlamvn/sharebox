package com.dinhlam.sharebox.ui.home.community

import android.content.Context
import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.VideoMixerDetail
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.pref.AppSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
    private val boxRepository: BoxRepository,
    private val appSharePref: AppSharePref,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val videoMixerRepository: VideoMixerRepository,
    private val videoHelper: VideoHelper,
) : BaseViewModel<CommunityState>(CommunityState(isRefreshing = true)) {

    init {
        getListBoxes()
        getCurrentActiveBox()
        consume(CommunityState::currentBox) {
            loadShares()
        }
    }

    private fun getListBoxes() = execute {
        val boxes = boxRepository.findLatestBoxWithoutPasscode()
        copy(boxes = boxes)
    }

    private fun getCurrentActiveBox() = execute {
        val boxId = appSharePref.getLatestActiveBoxId().takeIfNotNullOrBlank()
            ?: return@execute loadShares().let { this }
        val box = boxRepository.findOne(boxId) ?: return@execute loadShares().let { this }
        copy(currentBox = box, isRefreshing = false)
    }

    private fun loadShares() {
        setState { copy(isRefreshing = true) }
        execute {
            val shares = loadShares(currentBox, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
            val map = mutableMapOf<String, VideoMixerDetail>()
            for (i in shares.indices) {
                val videoMixer = videoMixerRepository.findOne(shares[i].shareId)
                videoMixer?.let { map[shares[i].shareId] = it }
            }

            copy(shares = shares, isRefreshing = false, videoMixers = videoMixers.plus(map))
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val shares = loadShares(
                currentBox,
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )

            val map = mutableMapOf<String, VideoMixerDetail>()
            for (i in shares.indices) {
                val videoMixer = videoMixerRepository.findOne(shares[i].shareId)
                videoMixer?.let { map[shares[i].shareId] = it }
            }

            copy(
                shares = this.shares.plus(shares),
                isLoadingMore = false,
                canLoadMore = shares.isNotEmpty(),
                currentPage = currentPage + 1,
                videoMixers = videoMixers.plus(map)
            )
        }
    }

    suspend fun loadShares(boxDetail: BoxDetail?, limit: Int, offset: Int): List<ShareDetail> {
        return boxDetail?.let { box ->
            shareRepository.findWhereInBox(
                box.boxId,
                limit,
                offset
            )
        } ?: shareRepository.findCommunityShares(limit, offset)
    }

    fun doOnRefresh() {
        setState { CommunityState() }
        getListBoxes()
        getCurrentActiveBox()
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

    fun showBookmarkCollectionPicker(shareId: String, @UiThread block: (String?) -> Unit) =
        doInBackground {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }

    fun setBox(box: BoxDetail?) = getState { state ->
        if (state.currentBox != box) {
            setState { copy(currentBox = box) }
            box?.let { nonNullBox ->
                doInBackground {
                    boxRepository.updateLastSeen(nonNullBox.boxId, nowUTCTimeInMillis())
                }
                appSharePref.setLatestActiveBoxId(nonNullBox.boxId)
            } ?: appSharePref.setLatestActiveBoxId("")
        }
    }

    fun setBox(boxId: String) {
        doInBackground {
            val boxDetail = boxRepository.findOne(boxId) ?: return@doInBackground setBox(null)
            setBox(boxDetail)
        }
    }

    fun saveVideoToGallery(context: Context, id: Int, videoSource: VideoSource, videoUri: String) {
        videoHelper.downloadVideo(context, id, videoSource, videoUri)
    }
}