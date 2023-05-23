package com.dinhlam.sharebox.ui.home.videomixer

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.model.VideoMixerDetail

data class VideoMixerState(
    val isRefreshing: Boolean = true,
    val videos: List<VideoMixerDetail> = emptyList()
) : BaseViewModel.BaseState