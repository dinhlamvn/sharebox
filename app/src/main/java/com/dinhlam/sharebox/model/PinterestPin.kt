package com.dinhlam.sharebox.model

data class PinterestPin(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
) {
    val url: String
        get() = "https://www.pinterest.com/pin/$id/"
}
