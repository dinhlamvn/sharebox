package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class TiktokDiscover(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("playCount")
    val playCount: Long,
    @SerializedName("commentCount")
    val commentCount: Long,
    @SerializedName("diggCount")
    val diggCount: Long,
    @SerializedName("shareCount")
    val shareCount: Long,
    @SerializedName("desc")
    val desc: String?,
)
