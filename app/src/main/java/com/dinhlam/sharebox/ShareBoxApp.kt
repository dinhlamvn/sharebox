package com.dinhlam.sharebox

import android.app.Application
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.repository.UserRepository
import com.dinhlam.sharebox.utils.IconUtils
import com.dinhlam.sharebox.utils.UserUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class ShareBoxApp : Application() {

    private val applicationScope = MainScope()

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var userSharePref: UserSharePref

    override fun onCreate() {
        super.onCreate()
        createFakeUser()
    }

    private fun createFakeUser() {
        applicationScope.launch(Dispatchers.IO) {
            val user = User(
                userId = UserUtils.fakeUserId,
                name = "William Lam",
                avatar = IconUtils.FAKE_AVATAR
            )
            val insertUser = userRepository.insert(user)

            Logger.debug("insert user $insertUser")
            userSharePref.setActiveUserId(user.userId)
        }
    }
}
