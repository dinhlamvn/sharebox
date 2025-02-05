package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.data.network.response.AppDLResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AppDLServices {

    @POST("1/fetch")
    @FormUrlEncoded
    suspend fun fetch(
        @Field("id") id: String,
        @Field("locale") locale: String,
        @Field("tt") tt: String,
        @Field("ts") ts: String,
    ): AppDLResponse?
}