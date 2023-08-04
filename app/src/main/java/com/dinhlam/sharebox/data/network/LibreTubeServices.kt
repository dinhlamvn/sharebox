package com.dinhlam.sharebox.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface LibreTubeServices {

    @GET("streams/{videoId}")
    @Headers(
        "content-type:application/x-www-form-urlencoded; charset=UTF-8",
    )
    suspend fun getDownloadLink(
        @Header("User-Agent") userAgent: String,
        @Path("videoId") videoId: String,
    ): Response<ResponseBody>

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody
}