package com.dinhlam.sharesaver.di

import com.dinhlam.sharesaver.json.ShareImageJsonSerializerDeserializer
import com.dinhlam.sharesaver.json.ShareTextJsonSerializerDeserializer
import com.dinhlam.sharesaver.json.ShareWebLinkJsonSerializerDeserializer
import com.dinhlam.sharesaver.ui.share.ShareState
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
@InstallIn(value = [SingletonComponent::class, ActivityComponent::class, FragmentComponent::class, ViewModelComponent::class])
object AppModule {

    @Provides
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            ShareState.ShareInfo.ShareText::class.java,
            ShareTextJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareState.ShareInfo.ShareImage::class.java,
            ShareImageJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareState.ShareInfo.ShareWebLink::class.java,
            ShareWebLinkJsonSerializerDeserializer()
        )
        return gsonBuilder.create()
    }
}
