package com.dinhlam.sharebox.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface FDownServices {

    @POST("download.php")
    @FormUrlEncoded
    suspend fun getDownloadData(@Field("URLz") facebookUrl: String): Response<ResponseBody>
}