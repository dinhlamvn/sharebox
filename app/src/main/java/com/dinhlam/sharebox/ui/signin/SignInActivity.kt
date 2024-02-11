package com.dinhlam.sharebox.ui.signin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.databinding.ActivitySignInBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
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

    private val avatarResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showAvatar(result.data)
            }
        }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), ::handleSignInResult)

    private val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

    private var customAvatarUri: Uri? = null

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var firebaseStorageHelper: FirebaseStorageHelper

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    @Inject
    lateinit var userRepository: UserRepository

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

        binding.textEditAvatar.setOnClickListener {
            avatarResultLauncher.launch(router.pickImageIntent())
        }

        binding.textEditAvatar.setDrawableCompat(end = Icons.editIcon(this) {
            copy(sizeDp = 16)
        })
    }

    private fun setupButtonForSignIn() {
        binding.buttonSignIn.setDrawableCompat(Icons.googleIcon(this))
        binding.buttonSignIn.setOnClickListener {
            requestSignIn()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CUSTOM_AVATAR_URI, customAvatarUri?.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        customAvatarUri = savedInstanceState.getString(KEY_CUSTOM_AVATAR_URI)?.let { str ->
            Uri.parse(str)
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
        activityScope.launch(Dispatchers.IO) {
            val userId = UserUtils.createUserId(email)
            val currentUser = userRepository.findOneRaw(userId)

            withContext(Dispatchers.Main) {
                val displayName = currentUser?.name.takeIfNotNullOrBlank() ?: user.displayName
                ?: return@withContext signOut()
                val photoUrl =
                    currentUser?.avatar.takeIfNotNullOrBlank() ?: user.photoUrl?.toString()
                    ?: return@withContext signOut()
                binding.buttonSignIn.setDrawableCompat(start = null)
                binding.buttonSignIn.setText(R.string.complete)

                ImageLoader.INSTANCE.load(this@SignInActivity, photoUrl, binding.imageAvatar) {
                    copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
                }

                binding.imageAppIcon.isVisible = false
                binding.imageAvatar.isVisible = true
                binding.textEditAvatar.isVisible = true
                binding.textLayoutName.isVisible = true
                binding.textTitleUserName.isVisible = true
                binding.textInputName.setText(displayName)

                binding.buttonSignIn.setOnClickListener {
                    val name = binding.textInputName.getTrimmedText()
                    createUser(email, name, photoUrl)
                }

                binding.textInputName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val name = binding.textInputName.getTrimmedText()
                        createUser(email, name, photoUrl)
                    }
                    true
                }
            }
        }
    }

    private fun createUser(email: String, name: String, photoUrl: String) {
        binding.viewLoading.show()
        activityScope.launch {
            val userId = UserUtils.createUserId(email)
            val avatarUrl = customAvatarUri?.let { uri ->
                firebaseStorageHelper.uploadUserAvatar(
                    userId, uri
                )
            } ?: photoUrl

            userHelper.createUser(userId, name, avatarUrl, { user ->
                realtimeDatabaseRepository.push(user)
                if (signInForResult) {
                    binding.viewLoading.hide()
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
        AuthUI.getInstance().signOut(this).addOnSuccessListener {
            showToast(R.string.logged_out)
            setupButtonForSignIn()
            binding.imageAppIcon.isVisible = true
            binding.imageAvatar.setImageDrawable(null)
            binding.imageAvatar.isVisible = false
            binding.textEditAvatar.isVisible = false
            customAvatarUri = null
            binding.textInputName.text = null
            binding.textLayoutName.isVisible = false
            binding.textTitleUserName.isVisible = false
            binding.viewLoading.hide()
        }
    }

    private fun goHome() {
        binding.viewLoading.hide()
        startActivity(
            router.home().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun showAvatar(data: Intent?) {
        val uri = data?.data ?: return
        customAvatarUri = uri
        ImageLoader.INSTANCE.load(this, uri, binding.imageAvatar) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
    }
}