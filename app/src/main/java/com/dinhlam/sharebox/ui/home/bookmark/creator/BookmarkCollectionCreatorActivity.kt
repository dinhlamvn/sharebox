package com.dinhlam.sharebox.ui.home.bookmark.creator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBookmarkCollectionCreatorBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkCollectionCreatorActivity :
    BaseViewModelActivity<BookmarkCollectionCreatorState, BookmarkCollectionCreatorViewModel, ActivityBookmarkCollectionCreatorBinding>() {

    override fun onCreateViewBinding(): ActivityBookmarkCollectionCreatorBinding {
        return ActivityBookmarkCollectionCreatorBinding.inflate(layoutInflater)
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
                Logger.debug(result.data?.getStringExtra(AppExtras.EXTRA_PASSCODE) ?: "No")
            }
        }

    @Inject
    lateinit var appRouter: AppRouter

    override val viewModel: BookmarkCollectionCreatorViewModel by viewModels()

    override fun onStateChanged(state: BookmarkCollectionCreatorState) {
        viewBinding.textErrorThumbnail.isVisible = state.errorThumbnail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewBinding.checkboxPasscode.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                passcodeResultLauncher.launch(appRouter.passcodeIntent(this))
            }
        }

        viewModel.consume(this, BookmarkCollectionCreatorState::success, true) { success ->
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

        viewModel.consume(this, BookmarkCollectionCreatorState::errorName, true) { errorRes ->
            errorRes?.let { res ->
                viewBinding.textEditName.error = getString(res)
                viewBinding.textEditName.requestFocus()
            } ?: viewBinding.textEditName.apply { text = null }
        }

        viewModel.consume(this, BookmarkCollectionCreatorState::errorDesc, true) { errorRes ->
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