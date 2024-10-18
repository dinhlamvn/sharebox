package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class TiktokCategory(
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("categoryName")
    val categoryName: String
)
