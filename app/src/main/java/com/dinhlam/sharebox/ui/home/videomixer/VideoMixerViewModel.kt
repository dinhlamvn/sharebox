package com.dinhlam.sharebox.ui.home.videomixer

import android.content.Context
import android.net.Uri
import androidx.annotation.UiThread
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.pref.AppSharePref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoMixerViewModel @Inject constructor(
    private val videoMixerRepository: VideoMixerRepository,
    private val likeRepository: LikeRepository,
    private val userHelper: UserHelper,
    private val bookmarkRepository: BookmarkRepository,
    private val boxRepository: BoxRepository,
    private val appSharePref: AppSharePref,
    private val localStorageHelper: LocalStorageHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : BaseViewModel<VideoMixerState>(VideoMixerState()) {

    init {
        getLatestBox()
        consume(VideoMixerState::currentBox) {
            loadVideoMixers()
        }
    }

    private fun getLatestBox() = execute {
        val boxId = appSharePref.getLatestActiveBoxId().takeIfNotNullOrBlank()
            ?: return@execute loadVideoMixers().let { this }
        val box = boxRepository.findOne(boxId) ?: return@execute loadVideoMixers().let { this }
        copy(currentBox = box, isRefreshing = false)
    }

    private fun loadVideoMixers() {
        setState { copy(isRefreshing = true) }
        execute {
            val videos = currentBox?.let { box ->
                videoMixerRepository.findWhereInBox(
                    box.boxId, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0
                )
            } ?: videoMixerRepository.find(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
            copy(videos = videos, isRefreshing = false)
        }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val videos = currentBox?.let { box ->
                videoMixerRepository.findWhereInBox(
                    box.boxId,
                    AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                    currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
                )
            } ?: videoMixerRepository.find(
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
            )
            copy(
                videos = this.videos.plus(videos),
                isLoadingMore = false,
                canLoadMore = videos.isNotEmpty(),
                currentPage = currentPage + 1
            )
        }
    }

    fun doOnPullRefresh() {
        setState { VideoMixerState() }
        getLatestBox()
    }

    fun like(shareId: String) = doInBackground {
        val result =
            likeRepository.like(shareId, userHelper.getCurrentUserId()) ?: return@doInBackground
        realtimeDatabaseRepository.push(result)
        setState {
            val videos = videos.map { videoDetail ->
                if (videoDetail.shareId == shareId) {
                    videoDetail.copy(
                        shareDetail = videoDetail.shareDetail.copy(
                            likeNumber = videoDetail.shareDetail.likeNumber + 1, liked = true
                        )
                    )
                } else {
                    videoDetail
                }
            }
            copy(videos = videos)
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
                        val videos = videos.map { videoDetail ->
                            if (videoDetail.shareId == shareId) {
                                videoDetail.copy(
                                    shareDetail = videoDetail.shareDetail.copy(
                                        bookmarked = true
                                    )
                                )
                            } else {
                                videoDetail
                            }
                        }
                        copy(videos = videos)
                    }
                }
            }
        } ?: run {
            val deleted = bookmarkRepository.delete(shareId)
            if (deleted) {
                setState {
                    val videos = videos.map { videoDetail ->
                        if (videoDetail.shareId == shareId) {
                            videoDetail.copy(shareDetail = videoDetail.shareDetail.copy(bookmarked = false))
                        } else {
                            videoDetail
                        }
                    }
                    copy(videos = videos)
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

    private fun setBox(box: BoxDetail?) = getState { state ->
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

    fun saveVideoToGallery(context: Context, videoUri: String) {
        doInBackground {
            try {
                localStorageHelper.saveVideoToGallery(context, Uri.parse(videoUri))
                postShowToast(R.string.success_save_video_to_gallery)
            } catch (e: Exception) {
                postShowToast(R.string.error_save_video_to_gallery)
            }
        }
    }
}