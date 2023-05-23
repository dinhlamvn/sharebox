package com.dinhlam.sharebox.ui.home.videomixer

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.VideoMixerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoMixerViewModel @Inject constructor(
    private val videoMixerRepository: VideoMixerRepository,
) : BaseViewModel<VideoMixerState>(VideoMixerState()) {


    init {
        loadVideoMixers()
    }

    private fun loadVideoMixers() = backgroundTask {
        setState { copy(isRefreshing = true) }
        val videos = videoMixerRepository.find()
        setState { copy(videos = videos, isRefreshing = false) }
    }

    fun doOnPullRefresh() {
        setState { VideoMixerState() }
        loadVideoMixers()
    }

}