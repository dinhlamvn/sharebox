package com.dinhlam.sharebox.helper

import android.content.Context
import com.google.net.cronet.okhttptransport.CronetCallFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Call
import org.chromium.net.CronetEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CronetHelper @Inject constructor(@ApplicationContext appContext: Context) {

    private val cronetEngine: CronetEngine = CronetEngine.Builder(appContext)
        .enableHttp2(true)
        .enableQuic(true)
        .enableBrotli(true)
        .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 1024L * 1024L * 10) // 10MB
        .build()

    private val _callFactory: CronetCallFactory by lazy {
        CronetCallFactory.newBuilder(cronetEngine).build()
    }
    val callFactory: Call.Factory
        get() = _callFactory
}