package com.dinhlam.sharebox.data.network.response

import com.google.gson.annotations.SerializedName

data class UnsplashSearchResponse(
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val results: List<UnsplashPhotoResponse>,
)

data class UnsplashPhotoResponse(
    val id: String,
    val width: Int,
    val height: Int,
    val urls: UnsplashUrlsResponse,
    val user: UnsplashUserResponse,
    val links: UnsplashPhotoLinksResponse,
)

data class UnsplashUrlsResponse(
    val regular: String,
    val small: String,
)

data class UnsplashUserResponse(
    val name: String,
    val username: String,
    val links: UnsplashUserLinksResponse,
)

data class UnsplashUserLinksResponse(
    val html: String,
)

data class UnsplashPhotoLinksResponse(
    val html: String,
    @SerializedName("download_location") val downloadLocation: String,
)

data class UnsplashDownloadResponse(
    val url: String,
)
