package com.dinhlam.sharebox.di

import com.dinhlam.sharebox.json.ShareImageJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareMultipleImageJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareTextJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareWebLinkJsonSerializerDeserializer
import com.dinhlam.sharebox.model.ShareData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(
    value = [
        SingletonComponent::class,
        ActivityComponent::class,
        FragmentComponent::class,
        ViewModelComponent::class
    ]
)
object AppModule {

    @Provides
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareText::class.java, ShareTextJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareImage::class.java, ShareImageJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareUrl::class.java, ShareWebLinkJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareImages::class.java, ShareMultipleImageJsonSerializerDeserializer()
        )
        return gsonBuilder.create()
    }
}
