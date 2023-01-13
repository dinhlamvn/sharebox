package com.dinhlam.sharebox

import android.R
import android.app.Application
import android.widget.Toast
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.utils.FolderUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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
        createDefaultFoldersOnFirstLaunch()

        syncFirebaseToken()
    }

    private fun createDefaultFoldersOnFirstLaunch() {
        if (!appSharePref.isAppFirstLaunch()) {
            applicationScope.launch(Dispatchers.IO) {
                folderRepository.insertMany(*FolderUtils.getDefaultFolders(this@ShareBoxApp).toTypedArray())
                appSharePref.commitAppFirstLaunch()
            }
        }
    }

    private fun syncFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.warning("Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Logger.debug("Here you token: $token")
        })
    }
}
