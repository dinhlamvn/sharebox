package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.data.network.response.LoveTikSearchResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface LoveTikServices {

    @POST("api/ajax/search")
    @FormUrlEncoded
    suspend fun getVideoDownloadUrl(@Field("query") videoUrl: String): LoveTikSearchResponse?

    @GET
    suspend fun get(@Url url: String): Call<ResponseBody>
}