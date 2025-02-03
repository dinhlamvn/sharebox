package com.dinhlam.sharebox.tracking.events

import com.dinhlam.sharebox.tracking.Event

data class YoutubeDownloadErrorEvent(
    val error: String?
) : Event {
    override val eventName: String
        get() = "youtube_download_error"

    override fun getEventParams(): Map<String, Any> {
        return mapOf("error" to (error ?: "Unknown"))
    }
}
