package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class BoxMember(
    @SerializedName("dataKey")
    val dataKey: String,
    @SerializedName("memberId")
    val memberId: String,
    @SerializedName("memberEmail")
    val memberEmail: String
)
