package com.dinhlam.sharebox.data.repository

import androidx.core.net.toUri
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.data.network.UnsplashServices
import com.dinhlam.sharebox.data.network.response.UnsplashPhotoResponse
import com.dinhlam.sharebox.model.UnsplashPhoto
import com.dinhlam.sharebox.storage.LocalStorageManager
import javax.inject.Inject

class UnsplashRepository @Inject constructor(
    private val services: UnsplashServices,
    private val localStorageManager: LocalStorageManager,
) {
    suspend fun search(query: String, page: Int, perPage: Int = 20): SearchResult {
        check(BuildConfig.UNSPLASH_ACCESS_KEY.isNotBlank()) {
            "Add UNSPLASH_ACCESS_KEY to local.properties to use Unsplash"
        }
        val response = services.searchPhotos(
            authorization = "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}",
            query = query,
            page = page,
            perPage = perPage,
        )
        return SearchResult(
            photos = response.results.map { it.toDomain() },
            totalPages = response.totalPages,
        )
    }

    suspend fun download(photo: UnsplashPhoto) {
        check(BuildConfig.UNSPLASH_ACCESS_KEY.isNotBlank()) {
            "Add UNSPLASH_ACCESS_KEY to local.properties to use Unsplash"
        }
        val download = services.trackDownload(
            downloadLocation = photo.downloadLocation,
            authorization = "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}",
        )
        localStorageManager.saveImageToGallery(download.url.toUri(), "Unsplash")
    }

    private fun UnsplashPhotoResponse.toDomain() = UnsplashPhoto(
        id = id,
        imageUrl = urls.small,
        previewUrl = urls.regular,
        downloadLocation = links.downloadLocation,
        width = width,
        height = height,
        photographerName = user.name,
        photographerUrl = withAttribution(user.links.html),
        photoUrl = withAttribution(links.html),
    )

    private fun withAttribution(url: String): String {
        val separator = if ('?' in url) '&' else '?'
        return "$url${separator}utm_source=sharebox&utm_medium=referral"
    }

    data class SearchResult(
        val photos: List<UnsplashPhoto>,
        val totalPages: Int,
    )
}
