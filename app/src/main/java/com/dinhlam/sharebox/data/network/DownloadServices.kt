package com.dinhlam.sharebox.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadServices {

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody
}