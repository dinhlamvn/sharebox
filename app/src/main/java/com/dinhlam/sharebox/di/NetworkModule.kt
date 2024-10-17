package com.dinhlam.sharebox.di

import android.content.Context
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.AppServices
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.data.network.FDownServices
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.helper.CronetHelper
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@Module
@InstallIn(
    value = [SingletonComponent::class, ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class]
)
object NetworkModule {

    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "okhttp_caches")
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
        return OkHttpClient.Builder()
            .connectTimeout(30_000, TimeUnit.MILLISECONDS)
            .readTimeout(30_000, TimeUnit.MILLISECONDS)
            .writeTimeout(30_000, TimeUnit.MILLISECONDS)
            .cache(Cache(cacheDir, 1024 * 1024 * 50)).build()

    }

    @Provides
    fun provideAppServices(
        gson: Gson, httpClient: OkHttpClient
    ): AppServices {
        return getRetrofitBuilder(gson, httpClient)
            .baseUrl("http://10.0.2.2:3000")
            .build().create(AppServices::class.java)
    }

    @Provides
    fun provideSSSTikServices(
        gson: Gson, httpClient: OkHttpClient
    ): SSSTikServices {
        return getRetrofitBuilder(gson, httpClient)
            .baseUrl(AppConsts.SSSTIK_SERVICE_BASE_URL)
            .build().create(SSSTikServices::class.java)
    }

    @Provides
    fun provideLibreTubeServices(
        gson: Gson, httpClient: OkHttpClient, cronetHelper: CronetHelper,
    ): LibreTubeServices {
        return getRetrofitBuilder(gson, httpClient)
            .callFactory(cronetHelper.callFactory)
            .baseUrl(AppConsts.LIBRE_TUBE_SERVICE_BASE_URL)
            .build().create(LibreTubeServices::class.java)
    }

    @Provides
    fun provideDownloadServices(
        gson: Gson, httpClient: OkHttpClient
    ): DownloadServices {
        return getRetrofitBuilder(gson, httpClient)
            .baseUrl("https://google.com")
            .build().create(DownloadServices::class.java)
    }

    @Provides
    fun provideFDownServices(
        gson: Gson, httpClient: OkHttpClient
    ): FDownServices {
        return getRetrofitBuilder(gson, httpClient)
            .baseUrl("https://fdown.net/")
            .build().create(FDownServices::class.java)
    }

    private fun getRetrofitBuilder(gson: Gson, httpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder().client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
    }
}