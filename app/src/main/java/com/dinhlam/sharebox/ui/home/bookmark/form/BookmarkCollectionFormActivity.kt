package com.dinhlam.sharebox.ui.home.bookmark.form

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBookmarkCollectionFormBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkCollectionFormActivity :
    BaseViewModelActivity<BookmarkCollectionFormState, BookmarkCollectionFormViewModel, ActivityBookmarkCollectionFormBinding>() {

    override fun onCreateViewBinding(): ActivityBookmarkCollectionFormBinding {
        return ActivityBookmarkCollectionFormBinding.inflate(layoutInflater)
    }

    private val thumbnailResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showThumbnail(result.data)
            }
        }

    private val passcodeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val passcode = result?.data?.getStringExtra(AppExtras.EXTRA_PASSCODE)
                    ?: return@registerForActivityResult
                viewModel.setPasscode(passcode)
                Logger.debug(passcode)
            }
        }

    @Inject
    lateinit var appRouter: AppRouter

    override val viewModel: BookmarkCollectionFormViewModel by viewModels()

    override fun onStateChanged(state: BookmarkCollectionFormState) {
        if (state.passcode.isEmpty()) {
            viewBinding.textLayoutPasscode.endIconDrawable = null
        } else {
            if (state.isPasscodeVisible) {
                viewBinding.textLayoutPasscode.setEndIconDrawable(R.drawable.ic_visibility)
                viewBinding.textEditPasscode.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                viewBinding.textLayoutPasscode.setEndIconDrawable(R.drawable.ic_visibility_off)
                viewBinding.textEditPasscode.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
        viewBinding.imageClear.isVisible = state.passcode.isNotEmpty()
        viewBinding.textLayoutPasscode.setEndIconActivated(state.passcode.isNotEmpty())
        viewBinding.textErrorThumbnail.isVisible = state.errorThumbnail
        viewBinding.textEditPasscode.setText(state.passcode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewBinding.imageClear.setOnClickListener {
            viewModel.clearPasscode()
        }

        viewBinding.textLayoutPasscode.setEndIconOnClickListener {
            viewModel.togglePasscodeVisibility()
        }

        viewBinding.textEditPasscode.setOnClickListener {
            passcodeResultLauncher.launch(appRouter.passcodeIntent(this))
        }

        viewModel.consume(this, BookmarkCollectionFormState::success, true) { success ->
            if (success) {
                returnResultOk()
            }
        }

        viewBinding.buttonThumbnail.setOnClickListener {
            requestPickThumbnail()
        }

        viewBinding.imageDone.setOnClickListener {
            val name = viewBinding.textEditName.getTrimmedText()
            val desc = viewBinding.textEditDesc.getTrimmedText()
            viewModel.createBookmarkCollection(this, name, desc)
        }

        viewBinding.textEditName.doAfterTextChanged { editable ->
            val text = editable.toString().takeIfNotNullOrBlank()
            viewModel.clearErrorName(text)
        }

        viewBinding.textEditDesc.doAfterTextChanged { editable ->
            val text = editable.toString().takeIfNotNullOrBlank()
            viewModel.clearErrorDesc(text)
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorName, true) { errorRes ->
            errorRes?.let { res ->
                viewBinding.textEditName.error = getString(res)
                viewBinding.textEditName.requestFocus()
            } ?: viewBinding.textEditName.apply { text = null }
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorDesc, true) { errorRes ->
            errorRes?.let { res ->
                viewBinding.textEditDesc.error = getString(res)
                viewBinding.textEditDesc.requestFocus()
            } ?: viewBinding.textEditDesc.apply { text = null }
        }
    }

    private fun returnResultOk() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun requestPickThumbnail() {
        thumbnailResultLauncher.launch(appRouter.pickImageIntent())
    }

    private fun showThumbnail(data: Intent?) {
        val uri = data?.data ?: return
        viewModel.setThumbnail(uri)
        ImageLoader.instance.load(this, uri, viewBinding.imageThumbnail)
    }
}