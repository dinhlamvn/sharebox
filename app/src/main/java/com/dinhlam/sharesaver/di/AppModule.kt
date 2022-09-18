package com.dinhlam.sharesaver.di

import com.dinhlam.sharesaver.json.ShareImageJsonSerializerDeserializer
import com.dinhlam.sharesaver.json.ShareTextJsonSerializerDeserializer
import com.dinhlam.sharesaver.ui.share.ShareData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(value = [ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class])
object AppModule {

    @Provides
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareInfo.ShareText::class.java,
            ShareTextJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareInfo.ShareImage::class.java,
            ShareImageJsonSerializerDeserializer()
        )
        return gsonBuilder.create()
    }
}