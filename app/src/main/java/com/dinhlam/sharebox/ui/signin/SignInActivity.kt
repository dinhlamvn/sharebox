package com.dinhlam.sharebox.ui.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.databinding.ActivitySignInBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.TransferDataHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.services.RealtimeServiceManager
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), ::handleSignInResult)

    private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var boxRepository: BoxRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var userSharePref: UserSharePref

    @Inject
    lateinit var transferDataHelper: TransferDataHelper

    @Inject
    lateinit var realtimeServiceManager: RealtimeServiceManager

    private val signInForResult by lazy {
        intent.getBooleanExtra(
            AppExtras.EXTRA_SIGN_IN_FOR_RESULT, false
        )
    }

    override fun onCreateViewBinding(): ActivitySignInBinding {
        return ActivitySignInBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.textPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        binding.textPrivacyPolicy.text = buildSpannedString {
            append(getString(R.string.app_policy_desc))
            append(" ")
            inSpans(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    router.moveToBrowser(AppConsts.PRIVACY_POLICY_URL)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = true
                }
            }) {
                append("Privacy Policy")
            }
        }

        binding.viewLoading.show()

        if (userHelper.isSignedIn()) {
            return goHome()
        }

        binding.viewLoading.hide()
        binding.buttonSignIn.isVisible = true
        AuthUI.getInstance().signOut(this)
        setupButtonForSignIn()
    }

    private fun setupButtonForSignIn() {
        binding.buttonSignIn.setDrawableCompat(Icons.googleIcon(this) { copy(colorRes = android.R.color.white) })
        binding.buttonSignIn.setOnClickListener {
            requestSignIn()
        }
    }

    private fun requestSignIn() {
        val signInIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .setTheme(R.style.AppTheme).setLogo(R.mipmap.ic_launcher).build()

        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("DinhLam", "this here")
            val user = FirebaseAuth.getInstance().currentUser
            createUserInfo(user?.email, user?.displayName, user?.photoUrl?.toString())
        } else {
            response?.error?.let { error ->
                Log.e("DinhLam", error.message, error)
                showToast(error.message)
            } ?: showToast("Error")
        }
    }

    private fun createUserInfo(email: String?, displayName: String?, photoUrl: String?) {
        binding.viewLoading.show()
        val userEmail = email ?: return signOut()
        activityScope.launch(Dispatchers.IO) {
            val userName = displayName ?: "User-$email"
            val userPhotoUrl = photoUrl ?: UserUtils.ANONYMOUS_AVATAR_URL
            try {
                createUser(userEmail, userName, userPhotoUrl)
            } catch (e: Exception) {
                Logger.error(e)

            } finally {
                withContext(Dispatchers.Main) {
                    binding.viewLoading.hide()
                }
            }
        }
    }

    private suspend fun createUser(email: String, name: String, photoUrl: String) {
        val userId = UserUtils.createUserId(email)
        val user = userHelper.createUser(userId, name, photoUrl)
        if (user != null) {
            realtimeDatabaseRepository.push(user)
            transferDataHelper.transferData(userSharePref.getAnonymousUserId(), user.userId)
            realtimeServiceManager.bindRealtimeService()
            if (signInForResult) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                withContext(Dispatchers.Main) {
                    goHome()
                }
            }
        } else {
            activityScope.launch(Dispatchers.Main) {
                showToast(R.string.create_user_error)
                signOut()
            }
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            showToast(R.string.logged_out)
        }
    }

    private fun goHome() {
        startActivity(
            router.home().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}