package com.dinhlam.sharebox.ui.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.databinding.ActivitySignInBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    companion object {
        private const val KEY_CUSTOM_AVATAR_URI = "custom-avatar-uri"
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), ::handleSignInResult)

    private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var firebaseStorageHelper: FirebaseStorageHelper

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
            FirebaseAuth.getInstance().currentUser?.let(::createUserInfo)
                ?: return showToast(com.firebase.ui.auth.R.string.fui_error_unknown)
        } else {
            response?.error?.let { error ->
                showToast(error.message)
            } ?: showToast(com.firebase.ui.auth.R.string.fui_error_unknown)
        }
    }

    private fun createUserInfo(user: FirebaseUser) {
        val email = user.email ?: return signOut()
        activityScope.launch(Dispatchers.IO) {
            val name = user.displayName ?: "User-$email"
            val photoUrl = user.photoUrl?.toString() ?: UserUtils.ANONYMOUS_AVATAR_URL
            createUser(email, name, photoUrl)
        }
    }

    private suspend fun createUser(email: String, name: String, photoUrl: String) {
        withContext(Dispatchers.Main) {
            binding.viewLoading.show()
        }
        val userId = UserUtils.createUserId(email)
        userHelper.createUser(userId, name, photoUrl, { user ->
            realtimeDatabaseRepository.push(user)
            transferData(user)
            if (signInForResult) {
                withContext(Dispatchers.Main) {
                    binding.viewLoading.hide()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else {
                withContext(Dispatchers.Main) {
                    goHome()
                }
            }
        }, {
            activityScope.launch(Dispatchers.Main) {
                showToast(R.string.create_user_error)
                signOut()
            }
        })
    }

    private suspend fun transferData(user: User) {
        shareRepository.transferData(userSharePref.getAnonymousUserId(), user.userId)
        boxRepository.transferData(userSharePref.getAnonymousUserId(), user.userId)
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            showToast(R.string.logged_out)
        }
    }

    private fun goHome() {
        binding.viewLoading.hide()
        startActivity(
            router.home().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}