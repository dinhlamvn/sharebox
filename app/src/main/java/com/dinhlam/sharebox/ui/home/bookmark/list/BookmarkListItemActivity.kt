package com.dinhlam.sharebox.ui.home.bookmark.list

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.ActivityBookmarkListItemBinding
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        getState(viewModel) { state ->
            if (state.isSharesLoading) {
                add(LoadingModelView("loading_share"))
                return@getState
            }

            if (state.shares.isEmpty()) {
                add(TextModelView("text_empty", "No shares"))
            } else {
                val models = state.shares.map { shareDetail ->
                    shareDetail.shareData.buildShareModelViews(
                        screenHeight(),
                        shareDetail.shareId,
                        shareDetail.shareDate,
                        shareDetail.shareNote,
                        shareDetail.user,
                        shareDetail.likeNumber,
                        commentNumber = shareDetail.commentNumber,
                        bookmarked = shareDetail.bookmarked,
                        liked = shareDetail.liked,
                        boxDetail = shareDetail.boxDetail,
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionLike = ::onLike,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark
                    )
                }
                models.forEach { model ->
                    add(model)
                    add(
                        SizedBoxModelView(
                            "divider_${model.modelId}",
                            height = 8.dp(),
                        )
                    )
                }
            }
        }
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

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: BookmarkListItemViewModel by viewModels()

    override fun onStateChanged(state: BookmarkListItemState) {
        shareAdapter.requestBuildModelViews()
    }

    private fun updateUi(bookmarkCollection: BookmarkCollectionDetail) {
        ImageLoader.INSTANCE.load(this, bookmarkCollection.thumbnail, viewBinding.imageTopBar)
        ImageLoader.INSTANCE.load(
            this, bookmarkCollection.thumbnail, viewBinding.imageThumbnailSmall
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
        viewBinding.toolbar.title = bookmarkCollection.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.consume(
            this, BookmarkListItemState::bookmarkCollection
        ) { bookmarkCollection ->
            bookmarkCollection?.let(::updateUi)
        }

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.appbar) { _, insets ->
            (viewBinding.toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            WindowInsetsCompat.CONSUMED
        }

        viewBinding.appbar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val eightyPercent = appBar.totalScrollRange * 0.8
            viewBinding.imageThumbnailSmall.alpha =
                verticalOffset.absoluteValue / eightyPercent.toFloat()
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewBinding.recyclerView.adapter = shareAdapter

        viewModel.consume(
            this, BookmarkListItemState::requestVerifyPasscode
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

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    this, shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(this, shareData.text)
            }

            else -> {}
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_confirm)
            .setMessage(R.string.dialog_confirm_remove_bookmark)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.removeBookmark(shareId)
            }.setNegativeButton(R.string.dialog_cancel, null).show()
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(supportFragmentManager, shareId)
    }
}