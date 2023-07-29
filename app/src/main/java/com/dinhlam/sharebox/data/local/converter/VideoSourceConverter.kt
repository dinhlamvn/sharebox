package com.dinhlam.sharebox.data.local.converter

import androidx.room.TypeConverter
import com.dinhlam.sharebox.model.VideoSource

class VideoSourceConverter {

    @TypeConverter
    fun videoSourceToString(videoSource: VideoSource): String {
        return videoSource.sourceName
    }

    @TypeConverter
    fun strToVideoSource(str: String): VideoSource {
        return when (str) {
            "youtube" -> VideoSource.Youtube
            "tiktok" -> VideoSource.Tiktok
            "facebook" -> VideoSource.Facebook
            else -> error("Error parse video source $str to VideoSource object")
        }
    }
}