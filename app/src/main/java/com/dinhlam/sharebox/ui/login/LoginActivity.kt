package com.dinhlam.sharebox.ui.login

import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.databinding.ActivityLoginBinding
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.UserUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {


    @Inject
    lateinit var userPref: UserSharePref

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    override fun onCreateViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLogged = userPref.getActiveUserId().isNotEmpty()

        if (isLogged) {
            return goHome()
        }

        viewBinding.buttonLogin.setOnClickListener {
            activityScope.launch {
                val number = Random.nextInt(200, 300)

                val user = User(
                    userId = UserUtils.createUserId("dinhlamvn353_$number@gmail.com"),
                    name = "Dinh Lam $number",
                    avatar = "https://i.pravatar.cc/$number",
                    joinDate = nowUTCTimeInMillis()
                )
                val insertUser = withContext(Dispatchers.IO) { userRepository.insert(user) }
                Logger.debug("insert user $insertUser")

                if (!insertUser) {
                    showToast("Can't create user")
                } else {
                    userPref.setActiveUserId(user.userId)
                    realtimeDatabaseRepository.push(user)
                    goHome()
                }
            }
        }
    }

    private fun goHome() {
        startActivity(
            appRouter.home()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}