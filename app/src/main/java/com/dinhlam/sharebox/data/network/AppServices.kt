package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.model.TiktokCategory
import com.dinhlam.sharebox.model.TiktokDiscover
import retrofit2.http.GET
import retrofit2.http.Path

interface AppServices {

    @GET("tiktok/categories")
    suspend fun getTiktokCategories(): List<TiktokCategory>

    @GET("tiktok/discover/{categoryId}")
    suspend fun getTiktokTrending(@Path("categoryId") categoryId: Int): List<TiktokDiscover>
}