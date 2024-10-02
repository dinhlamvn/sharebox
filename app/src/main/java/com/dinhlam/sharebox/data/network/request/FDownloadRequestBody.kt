package com.dinhlam.sharebox.data.network.request

import com.google.gson.annotations.SerializedName

data class FDownloadRequestBody(
    @SerializedName("URLz")
    val facebookUrl: String
)