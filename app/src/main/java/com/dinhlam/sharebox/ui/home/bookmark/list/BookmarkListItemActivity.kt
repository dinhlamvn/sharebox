package com.dinhlam.sharebox.ui.home.bookmark.list

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsControllerCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.databinding.ActivityBookmarkListItemBinding
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class BookmarkListItemActivity :
    BaseViewModelActivity<BookmarkListItemState, BookmarkListItemViewModel, ActivityBookmarkListItemBinding>() {

    override fun onCreateViewBinding(): ActivityBookmarkListItemBinding {
        return ActivityBookmarkListItemBinding.inflate(layoutInflater)
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        add(TextModelView("text_empty", "No shares"))
    }

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.markPasscodeVerified()
            } else {
                finish()
            }
        }

    @Inject
    lateinit var appRouter: AppRouter

    override val viewModel: BookmarkListItemViewModel by viewModels()

    override fun onStateChanged(state: BookmarkListItemState) {
        state.bookmarkCollection?.let(::updateUi)
        shareAdapter.requestBuildModelViews()
    }

    private fun updateUi(bookmarkCollection: BookmarkCollectionDetail) {
        ImageLoader.instance.load(this, bookmarkCollection.thumbnail, viewBinding.imageTopBar)
        ImageLoader.instance.load(
            this, bookmarkCollection.thumbnail, viewBinding.imageThumbnailSmall
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
        viewBinding.toolbar.title = bookmarkCollection.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.appbar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val eightyPercent = appBar.totalScrollRange * 0.8
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                verticalOffset.absoluteValue >= eightyPercent
            viewBinding.imageThumbnailSmall.alpha =
                verticalOffset.absoluteValue / eightyPercent.toFloat()
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewBinding.recyclerView.adapter = shareAdapter

        viewModel.consume(
            this,
            BookmarkListItemState::requestVerifyPasscode,
            true
        ) { shouldRequest ->
            if (shouldRequest) {
                requestVerifyPasscode()
            }
        }
    }

    private fun requestVerifyPasscode() = getState(viewModel) { state ->
        val passcode = state.bookmarkCollection?.passcode.takeIfNotNullOrBlank() ?: return@getState
        val name = state.bookmarkCollection?.name ?: ""
        val intent = appRouter.passcodeIntent(this, passcode)
        intent.putExtra(
            AppExtras.EXTRA_PASSCODE_DESCRIPTION,
            getString(R.string.dialog_bookmark_collection_picker_verify_passcode, name)
        )
        passcodeConfirmResultLauncher.launch(intent)
    }
}