package com.dinhlam.sharesaver.ui.setting

import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseActivity
import com.dinhlam.sharesaver.databinding.ActivitySettingBinding
import com.dinhlam.sharesaver.extensions.showToast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class SettingActivity : BaseActivity<ActivitySettingBinding>(), FacebookCallback<LoginResult> {

    private val facebookCallback = CallbackManager.Factory.create()

    override fun onCreateViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().registerCallback(facebookCallback, this)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallback.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSuccess(result: LoginResult?) {
        val profile = Profile.getCurrentProfile()
        val text = getString(
            R.string.login_facebook_success,
            "${profile.firstName} ${profile.lastName}"
        )
        showToast(text)
    }

    override fun onCancel() {
        showToast(R.string.request_login_facebook)
    }

    override fun onError(error: FacebookException?) {
        showToast(R.string.login_facebook_error)
    }
}