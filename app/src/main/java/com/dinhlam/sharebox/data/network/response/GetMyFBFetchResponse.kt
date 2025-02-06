package com.dinhlam.sharebox.data.network.response

import com.google.gson.annotations.SerializedName

data class GetMyFBFetchResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("url")
    val url: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("sd")
    val sd: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("hd")
    val hd: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("is_private")
    val isPrivate: Boolean,
    @SerializedName("from_cdn")
    val fromCdn: Boolean,
    @SerializedName("is_live_streaming")
    val isLiveStreaming: Boolean,
    @SerializedName("is_url_from_message")
    val isUrlFromMessage: Boolean,
)
