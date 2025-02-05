package com.dinhlam.sharebox.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface SSSTikServices {

    @POST("abc?url=dl")
    suspend fun getDownloadLink(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody
}