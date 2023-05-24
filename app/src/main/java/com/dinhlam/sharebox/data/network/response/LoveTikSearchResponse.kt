package com.dinhlam.sharebox.data.network.response

import com.google.gson.annotations.SerializedName

data class LoveTikSearchResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("vid")
    val videoId: String,
    @SerializedName("links")
    val links: List<Link>
) {

    data class Link(
        @SerializedName("t")
        val type: String,
        @SerializedName("s")
        val watermarkInfo: String,
        @SerializedName("a")
        val downloadLink: String
    )
}
