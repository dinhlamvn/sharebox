package com.dinhlam.sharebox.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SSSTikServices {

    @POST("abc?url=dl")
    @Headers(
        "content-type:application/x-www-form-urlencoded; charset=UTF-8",
        "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    )
    suspend fun getDownloadLink(@Body requestBody: RequestBody): Response<ResponseBody>
}