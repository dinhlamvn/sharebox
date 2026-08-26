package com.dinhlam.sharebox.model

data class UnsplashPhoto(
    val id: String,
    val imageUrl: String,
    val downloadLocation: String,
    val width: Int,
    val height: Int,
    val photographerName: String,
    val photographerUrl: String,
    val photoUrl: String,
    val previewUrl: String = imageUrl,
)
