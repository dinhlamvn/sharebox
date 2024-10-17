package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.model.TiktokDiscover
import retrofit2.http.GET

interface AppServices {

    @GET("tiktok/discover")
    suspend fun getTiktokTrending(): List<TiktokDiscover>
}