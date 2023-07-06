package com.dinhlam.sharebox.ui.home.videomixer

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.data.model.VideoMixerDetail

data class VideoMixerState(
    val isRefreshing: Boolean = true,
    val videos: List<VideoMixerDetail> = emptyList(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val currentBox: BoxDetail? = null,
) : BaseViewModel.BaseState