package com.dinhlam.sharesaver

import android.app.Application
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.pref.AppSharePref
import com.dinhlam.sharesaver.repository.FolderRepository
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private val applicationScope = MainScope()

    @Inject
    lateinit var folderRepository: FolderRepository

    @Inject
    lateinit var appSharePref: AppSharePref

    override fun onCreate() {
        super.onCreate()
        initializeFacebook()
        createDefaultFoldersOnFirstLaunch()
    }

    private fun initializeFacebook() {
        AppEventsLogger.activateApp(this)
    }

    private fun createDefaultFoldersOnFirstLaunch() {
        if (!appSharePref.isAppFirstLaunch()) {
            applicationScope.launch(Dispatchers.IO) {
                val defaultFolders = listOf(
                    Folder("folder_home", "Home", "For all"),
                    Folder("folder_text", "Texts", "For plain text share"),
                    Folder("folder_web", "Webs", "For web link share"),
                    Folder("folder_image", "Images", "For image share")
                )
                folderRepository.insertMany(*defaultFolders.toTypedArray())
                appSharePref.commitAppFirstLaunch()
            }
        }
    }
}
