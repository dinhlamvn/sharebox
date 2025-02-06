package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.data.network.response.GetMyFBFetchResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GetMyFBServices {

    @POST("v1/fetch")
    @FormUrlEncoded
    suspend fun fetch(@Field("url") url: String): GetMyFBFetchResponse
}