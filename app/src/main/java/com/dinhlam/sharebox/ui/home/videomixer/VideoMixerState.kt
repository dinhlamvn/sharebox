package com.dinhlam.sharebox.ui.home.videomixer

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.VideoMixerDetail

data class VideoMixerState(
    val isRefreshing: Boolean = true,
    val videos: List<VideoMixerDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val currentBox: BoxDetail? = null,
    val showLoading: Boolean = false,
) : BaseViewModel.BaseState