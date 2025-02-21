package com.dinhlam.sharebox.di

import android.content.Context
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.network.AppDLServices
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.data.network.FDownServices
import com.dinhlam.sharebox.data.network.GetMyFBServices
import com.dinhlam.sharebox.data.network.LibreTubeServices
import com.dinhlam.sharebox.data.network.SSSTikServices
import com.dinhlam.sharebox.data.network.TiktokServices
import com.dinhlam.sharebox.data.network.response.AppDLResponse
import com.dinhlam.sharebox.di.qualifier.UserAgentInterceptor
import com.dinhlam.sharebox.helper.CronetHelper
import com.dinhlam.sharebox.json.AppDLResponseJsonDeserializer
import com.dinhlam.sharebox.utils.UserAgentUtils
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@Module
@InstallIn(
    value = [SingletonComponent::class]
)
object NetworkModule {

    @Provides
    @UserAgentInterceptor
    fun provideUserAgentInterceptor(): Interceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("User-Agent", UserAgentUtils.pickRandomUserAgent())
        chain.proceed(requestBuilder.build())
    }

    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "okhttp_caches")
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
        return OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEV) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .connectTimeout(30_000, TimeUnit.MILLISECONDS)
            .readTimeout(30_000, TimeUnit.MILLISECONDS)
            .writeTimeout(30_000, TimeUnit.MILLISECONDS)
            .cache(Cache(cacheDir, 1024 * 1024 * 50)).build()
    }

    @Provides
    fun provideSSSTikServices(
        gson: Gson,
        httpClient: OkHttpClient,
        @UserAgentInterceptor userAgentInterceptor: Interceptor
    ): SSSTikServices {
        return getRetrofitBuilder(
            gson,
            httpClient.newBuilder()
                .addInterceptor(userAgentInterceptor)
                .build()
        ).baseUrl(AppConsts.SSSTIK_SERVICE_BASE_URL)
            .build().create(SSSTikServices::class.java)
    }

    @Provides
    fun provideLibreTubeServices(
        gson: Gson,
        httpClient: OkHttpClient,
        cronetHelper: CronetHelper,
        @UserAgentInterceptor userAgentInterceptor: Interceptor
    ): LibreTubeServices {
        return getRetrofitBuilder(
            gson,
            httpClient.newBuilder().addInterceptor(userAgentInterceptor).build()
        ).callFactory(cronetHelper.callFactory)
            .baseUrl(AppConsts.LIBRE_TUBE_SERVICE_BASE_URL)
            .build().create(LibreTubeServices::class.java)
    }

    @Provides
    fun provideDownloadServices(
        gson: Gson,
        httpClient: OkHttpClient,
        @UserAgentInterceptor userAgentInterceptor: Interceptor
    ): DownloadServices {
        return getRetrofitBuilder(
            gson,
            httpClient.newBuilder().addInterceptor(userAgentInterceptor).build()
        ).baseUrl("https://google.com")
            .build().create(DownloadServices::class.java)
    }

    @Provides
    fun provideFDownServices(
        gson: Gson,
        httpClient: OkHttpClient,
        @UserAgentInterceptor userAgentInterceptor: Interceptor
    ): FDownServices {
        return getRetrofitBuilder(
            gson,
            httpClient.newBuilder().addInterceptor(userAgentInterceptor).build()
        ).baseUrl("https://fdown.net/")
            .build().create(FDownServices::class.java)
    }

    @Provides
    fun provideTiktokServices(
        gson: Gson, httpClient: OkHttpClient
    ): TiktokServices {
        return getRetrofitBuilder(gson, httpClient)
            .baseUrl("https://www.tiktok.com")
            .build().create(TiktokServices::class.java)
    }

    @Provides
    fun provideAppDLServices(
        gson: Gson, httpClient: OkHttpClient
    ): AppDLServices {
        val gsonBuilder = gson.newBuilder()
        gsonBuilder.registerTypeAdapter(
            AppDLResponse::class.java,
            AppDLResponseJsonDeserializer(gson)
        )
        return getRetrofitBuilder(
            gsonBuilder.create(),
            httpClient.newBuilder().addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader(
                    "User-Agent",
                    "ssstik.io/1.136/10.0.2.15/(com.video.videodownloader_appdl)"
                )
                requestBuilder.addHeader(
                    "Authorization",
                    "d9a97b094b5a1cdbfaab98d117031de5f01e4faec165c5a6bdc452d1a52fc268"
                )
                chain.proceed(requestBuilder.build())
            }.build()
        )
            .baseUrl("https://appdl.pro")
            .build()
            .create(AppDLServices::class.java)
    }

    @Provides
    fun provideGetMyFBServices(
        gson: Gson, httpClient: OkHttpClient
    ): GetMyFBServices {
        return getRetrofitBuilder(gson, httpClient.newBuilder().addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader(
                "token",
                "6639e1d16702e8a25265c6bbcd13e6dcbd9079c3"
            )
            chain.proceed(requestBuilder.build())
        }.build())
            .baseUrl("https://api.getmyfb.com")
            .build()
            .create(GetMyFBServices::class.java)
    }

    private fun getRetrofitBuilder(gson: Gson, httpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder().client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
    }
}