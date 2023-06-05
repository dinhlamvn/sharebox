package com.dinhlam.sharebox.ui.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivitySignInBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.IconUtils
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), ::handleSignInResult)

    private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var appRouter: AppRouter

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

        viewBinding.viewLoading.show()

        if (userHelper.isSignedIn()) {
            return goHome()
        }

        viewBinding.buttonSignIn.setDrawableCompat(IconUtils.googleIconLight(this))

        viewBinding.viewLoading.hide()
        viewBinding.buttonSignIn.isVisible = true
        AuthUI.getInstance().signOut(this)
        viewBinding.buttonSignIn.setOnClickListener {
            requestSignIn()
        }
    }

    private fun requestSignIn() {
        val signInIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .setTheme(R.style.AppTheme).setLogo(R.drawable.ic_launcher_foreground).build()

        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            FirebaseAuth.getInstance().currentUser?.let(::renderUserInfo)
                ?: return showToast(R.string.fui_error_unknown)
        } else {
            response?.error?.let { error ->
                showToast(error.message)
            } ?: showToast(R.string.fui_error_unknown)
        }
    }

    private fun renderUserInfo(user: FirebaseUser) {
        val email = user.email ?: return signOut()
        val displayName = user.displayName ?: return signOut()
        val photoUrl = user.photoUrl?.toString() ?: return signOut()
        viewBinding.buttonSignIn.isVisible = false

        ImageLoader.instance.load(this, photoUrl, viewBinding.imageAvatar) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }

        viewBinding.textLayoutName.isVisible = true
        viewBinding.textTitleUserName.isVisible = true
        viewBinding.textInputName.setText(displayName)

        viewBinding.textLayoutName.setEndIconOnClickListener {
            val name = viewBinding.textInputName.getTrimmedText()
            createUser(email, name, photoUrl)
        }

        viewBinding.textInputName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = viewBinding.textInputName.getTrimmedText()
                createUser(email, name, photoUrl)
            }
            true
        }
    }

    private fun createUser(email: String, name: String, photoUrl: String) {
        viewBinding.viewLoading.show()
        activityScope.launch {
            userHelper.createUser(email, name, photoUrl, {
                if (signInForResult) {
                    viewBinding.viewLoading.hide()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    goHome()
                }
            }, {
                showToast(R.string.create_user_error)
                signOut()
            })
        }
    }

    private fun signOut() {
        viewBinding.textInputName.text = null
        viewBinding.textLayoutName.isVisible = false
        viewBinding.textTitleUserName.isVisible = false
        viewBinding.buttonSignIn.isVisible = true
        viewBinding.viewLoading.hide()
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            showToast(R.string.logged_out)
        }
    }

    private fun goHome() {
        viewBinding.viewLoading.hide()
        startActivity(
            appRouter.home()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}