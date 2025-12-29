package com.dinhlam.sharebox.di

import android.content.Context
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.ShareBoxApp
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.json.ShareCheckListJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareFileJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareImageJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareImagesJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareNotificationJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareTextJsonSerializerDeserializer
import com.dinhlam.sharebox.json.ShareUrlJsonSerializerDeserializer
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.router.Router
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(
    value = [
        SingletonComponent::class,
    ]
)
object AppModule {

    @Provides
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareText::class.java, ShareTextJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareImage::class.java, ShareImageJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareUrl::class.java, ShareUrlJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareImages::class.java, ShareImagesJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareFile::class.java, ShareFileJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareCheckList::class.java, ShareCheckListJsonSerializerDeserializer
        )
        gsonBuilder.registerTypeAdapter(
            ShareData.ShareNotification::class.java, ShareNotificationJsonSerializerDeserializer
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

    @Provides
    fun provideRouter(@ApplicationContext context: Context): Router {
        return AppRouter(context)
    }

    @Provides
    fun provideApplicationScope(@ApplicationContext context: Context): CoroutineScope {
        return context.cast<ShareBoxApp>()!!
    }
}
