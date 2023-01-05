package com.dinhlam.sharebox

import android.app.Application
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.utils.FolderUtils
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ShareBoxApp : Application() {

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
                folderRepository.insertMany(*FolderUtils.getDefaultFolders().toTypedArray())
                appSharePref.commitAppFirstLaunch()
            }
        }
    }
}
