package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class TiktokDiscover(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("playCount")
    val playCount: Long,
    @SerializedName("desc")
    val desc: String?,
)
