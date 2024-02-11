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
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBookmarkCollectionFormBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
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
    lateinit var router: Router

    override val viewModel: BookmarkCollectionFormViewModel by viewModels()

    override fun onStateChanged(state: BookmarkCollectionFormState) {
        if (state.passcode.isNullOrBlank()) {
            binding.textLayoutPasscode.endIconDrawable = null
        } else {
            if (state.isPasscodeVisible) {
                binding.textLayoutPasscode.endIconDrawable = Icons.visibilityOnIcon(this)
                binding.textEditPasscode.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.textLayoutPasscode.endIconDrawable = Icons.visibilityOffIcon(this)
                binding.textEditPasscode.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
        binding.imageClear.isVisible = !state.passcode.isNullOrBlank()
        binding.textLayoutPasscode.setEndIconActivated(!state.passcode.isNullOrBlank())
        binding.textErrorThumbnail.isVisible = state.errorThumbnail
        binding.textEditPasscode.setText(state.passcode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.imageClear.setImageDrawable(Icons.clearIcon(this) {
            copy(sizeDp = 16)
        })

        binding.toolbar.navigationIcon = Icons.leftArrowIcon(this) {
            copy(sizeDp = 16)
        }

        binding.imageDone.setImageDrawable(Icons.doneIcon(this) {
            copy(sizeDp = 16)
        })

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.imageClear.setOnClickListener {
            viewModel.clearPasscode()
        }

        binding.textLayoutPasscode.setEndIconOnClickListener {
            viewModel.togglePasscodeVisibility()
        }

        binding.textEditPasscode.setOnClickListener {
            passcodeResultLauncher.launch(router.passcodeIntent(this))
        }

        viewModel.consume(this, BookmarkCollectionFormState::success) { success ->
            if (success) {
                returnResultOk()
            }
        }

        binding.buttonThumbnail.setOnClickListener {
            requestPickThumbnail()
        }

        binding.imageDone.setOnClickListener {
            val name = binding.textEditName.getTrimmedText()
            val desc = binding.textEditDesc.getTrimmedText()
            viewModel.performActionDone(this, name, desc)
        }

        binding.textEditName.doAfterTextChanged { editable ->
            viewModel.clearErrorName(editable.trimmedString())
        }

        binding.textEditDesc.doAfterTextChanged { editable ->
            viewModel.clearErrorDesc(editable.trimmedString())
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorName) { errorRes ->
            errorRes?.let { res ->
                binding.textEditName.error = getString(res)
                binding.textEditName.requestFocus()
            } ?: binding.textEditName.apply { error = null }
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorDesc) { errorRes ->
            errorRes?.let { res ->
                binding.textEditDesc.error = getString(res)
                binding.textEditDesc.requestFocus()
            } ?: binding.textEditDesc.apply { error = null }
        }

        getState(viewModel) { state ->
            state.bookmarkCollectionDetail?.let { collectionDetail ->
                ImageLoader.INSTANCE.load(
                    this,
                    collectionDetail.thumbnail,
                    binding.imageThumbnail
                ) {
                    copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
                }
                binding.textEditName.setText(collectionDetail.name)
                binding.textEditDesc.setText(collectionDetail.desc)
                binding.textEditPasscode.setText(collectionDetail.passcode)
            }
        }
    }

    private fun returnResultOk() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun requestPickThumbnail() {
        thumbnailResultLauncher.launch(router.pickImageIntent())
    }

    private fun showThumbnail(data: Intent?) {
        val uri = data?.data ?: return
        viewModel.setThumbnail(uri)
        ImageLoader.INSTANCE.load(this, uri, binding.imageThumbnail) {
            copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
        }
    }
}