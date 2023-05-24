package com.dinhlam.sharebox.data.mapper

import com.dinhlam.sharebox.data.local.entity.VideoMixer
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.data.model.VideoMixerDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoMixerToVideoMixerDetailMapper @Inject constructor() {
    fun map(videoMixer: VideoMixer, shareDetail: ShareDetail): VideoMixerDetail {
        return VideoMixerDetail(
            videoMixer.id,
            videoMixer.shareId,
            videoMixer.originalUrl,
            videoMixer.source,
            videoMixer.sourceId,
            videoMixer.uri,
            shareDetail
        )
    }
}