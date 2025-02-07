package com.dinhlam.sharebox.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SSSTikServices {

    @POST("abc?url=dl")
    suspend fun getDownloadLink(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>
}