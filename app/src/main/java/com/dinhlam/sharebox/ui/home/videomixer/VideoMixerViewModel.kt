package com.dinhlam.sharebox.ui.home.videomixer

import androidx.annotation.UiThread
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.UserHelper
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
) : BaseViewModel<VideoMixerState>(VideoMixerState()) {


    init {
        loadVideoMixers()
    }

    private fun loadVideoMixers() = backgroundTask {
        setState { copy(isRefreshing = true) }
        val videos = videoMixerRepository.find(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 0)
        setState { copy(videos = videos, isRefreshing = false) }
    }

    fun loadMores() {
        setState { copy(isLoadingMore = true) }
        execute {
            val videos = videoMixerRepository.find(
                AppConsts.LOADING_LIMIT_ITEM_PER_PAGE,
                offset = currentPage * AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
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
        loadVideoMixers()
    }

    fun like(shareId: String) = backgroundTask {
        if (likeRepository.likeAndSyncToCloud(shareId, userHelper.getCurrentUserId())) {
            setState {
                val videos = videos.map { videoDetail ->
                    if (videoDetail.shareId == shareId) {
                        videoDetail.copy(
                            shareDetail = videoDetail.shareDetail.copy(
                                likeNumber = videoDetail.shareDetail.likeNumber + 1,
                                liked = true
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

    fun bookmark(shareId: String, bookmarkCollectionId: String?) = backgroundTask {
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
        backgroundTask {
            val bookmarkDetail = bookmarkRepository.findOne(shareId)
            withContext(Dispatchers.Main) {
                block(bookmarkDetail?.bookmarkCollectionId)
            }
        }
}