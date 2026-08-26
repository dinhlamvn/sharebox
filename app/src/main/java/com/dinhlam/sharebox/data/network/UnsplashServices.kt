package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.data.network.response.UnsplashSearchResponse
import com.dinhlam.sharebox.data.network.response.UnsplashDownloadResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashServices {
    @Headers("Accept-Version: v1")
    @GET("search/photos")
    suspend fun searchPhotos(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): UnsplashSearchResponse

    @Headers("Accept-Version: v1")
    @GET
    suspend fun trackDownload(
        @retrofit2.http.Url downloadLocation: String,
        @Header("Authorization") authorization: String,
    ): UnsplashDownloadResponse
}
