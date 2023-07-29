package com.dinhlam.sharebox.di

import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.json.ShareImageJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareImagesJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareTextJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareUrlJsonSerializerDeserializer
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
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
            ShareData.ShareUrl::class.java, ShareUrlJsonSerializerDeserializer()
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareImages::class.java, ShareImagesJsonSerializerDeserializer()
        )
        return gsonBuilder.create()
    }

    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database(BuildConfig.FIREBASE_DATABASE_URL)
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance(BuildConfig.FIREBASE_STORAGE_URL)
    }
}
