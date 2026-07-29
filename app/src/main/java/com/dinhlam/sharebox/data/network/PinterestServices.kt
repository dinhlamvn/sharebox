package com.dinhlam.sharebox.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface PinterestServices {

    @GET
    suspend fun search(
        @Url url: String,
        @Header("User-Agent") userAgent: String,
        @Header("Accept-Language") language: String = "en-US,en;q=0.9",
    ): ResponseBody
}
