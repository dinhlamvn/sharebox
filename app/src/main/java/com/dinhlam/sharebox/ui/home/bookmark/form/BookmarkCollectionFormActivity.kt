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
import com.dinhlam.sharebox.utils.IconUtils
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
            viewBinding.textLayoutPasscode.endIconDrawable = null
        } else {
            if (state.isPasscodeVisible) {
                viewBinding.textLayoutPasscode.endIconDrawable = IconUtils.visibilityOnIcon(this)
                viewBinding.textEditPasscode.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                viewBinding.textLayoutPasscode.endIconDrawable = IconUtils.visibilityOffIcon(this)
                viewBinding.textEditPasscode.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
        viewBinding.imageClear.isVisible = !state.passcode.isNullOrBlank()
        viewBinding.textLayoutPasscode.setEndIconActivated(!state.passcode.isNullOrBlank())
        viewBinding.textErrorThumbnail.isVisible = state.errorThumbnail
        viewBinding.textEditPasscode.setText(state.passcode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.imageClear.setImageDrawable(IconUtils.clearIcon(this) {
            copy(sizeDp = 16)
        })

        viewBinding.toolbar.navigationIcon = IconUtils.leftArrowIcon(this) {
            copy(sizeDp = 16)
        }

        viewBinding.imageDone.setImageDrawable(IconUtils.doneIcon(this) {
            copy(sizeDp = 16)
        })

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
            passcodeResultLauncher.launch(router.passcodeIntent(this))
        }

        viewModel.consume(this, BookmarkCollectionFormState::success) { success ->
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
            viewModel.performActionDone(this, name, desc)
        }

        viewBinding.textEditName.doAfterTextChanged { editable ->
            viewModel.clearErrorName(editable.trimmedString())
        }

        viewBinding.textEditDesc.doAfterTextChanged { editable ->
            viewModel.clearErrorDesc(editable.trimmedString())
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorName) { errorRes ->
            errorRes?.let { res ->
                viewBinding.textEditName.error = getString(res)
                viewBinding.textEditName.requestFocus()
            } ?: viewBinding.textEditName.apply { error = null }
        }

        viewModel.consume(this, BookmarkCollectionFormState::errorDesc) { errorRes ->
            errorRes?.let { res ->
                viewBinding.textEditDesc.error = getString(res)
                viewBinding.textEditDesc.requestFocus()
            } ?: viewBinding.textEditDesc.apply { error = null }
        }

        getState(viewModel) { state ->
            state.bookmarkCollectionDetail?.let { collectionDetail ->
                ImageLoader.INSTANCE.load(
                    this,
                    collectionDetail.thumbnail,
                    viewBinding.imageThumbnail
                ) {
                    copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
                }
                viewBinding.textEditName.setText(collectionDetail.name)
                viewBinding.textEditDesc.setText(collectionDetail.desc)
                viewBinding.textEditPasscode.setText(collectionDetail.passcode)
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
        ImageLoader.INSTANCE.load(this, uri, viewBinding.imageThumbnail) {
            copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
        }
    }
}