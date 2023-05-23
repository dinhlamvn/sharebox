package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.VideoMixerDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoMixerRepository @Inject constructor(
    private val videoMixerDao: VideoMixerDao
) {
    
}