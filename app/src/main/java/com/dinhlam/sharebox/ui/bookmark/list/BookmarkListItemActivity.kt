package com.dinhlam.sharebox.ui.bookmark.list

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
import com.dinhlam.sharebox.databinding.ActivityBookmarkListItemBinding
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class BookmarkListItemActivity :
    BaseViewModelActivity<BookmarkListItemState, BookmarkListItemViewModel, ActivityBookmarkListItemBinding>(),
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener {

    override fun onCreateViewBinding(): ActivityBookmarkListItemBinding {
        return ActivityBookmarkListItemBinding.inflate(layoutInflater)
    }

    private val openShareTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.refresh()
            }
        }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isSharesLoading) {
                LoadingListModel("loading_share").attachTo(this)
                return@getState
            }

            if (state.shares.isEmpty()) {
                TextListModel("text_empty", getString(R.string.no_result)).attachTo(this)
            } else {
                state.shares.forEach { shareDetail ->
                    shareDetail.buildListItemListModel(::showMore, ::openShare)
                        .attachTo(this)
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
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: BookmarkListItemViewModel by viewModels()

    override fun onStateChanged(state: BookmarkListItemState) {
        shareAdapter.requestBuildListModels()
    }

    private fun updateUi(bookmarkCollection: BookmarkCollectionDetail) {
        ImageLoader.INSTANCE.load(this, bookmarkCollection.thumbnail, binding.imageTopBar)
        ImageLoader.INSTANCE.load(
            this, bookmarkCollection.thumbnail, binding.imageThumbnailSmall
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
        binding.toolbar.title = bookmarkCollection.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.navigationIcon = Icons.leftArrowIcon(this)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.consume(
            this, BookmarkListItemState::bookmarkCollection
        ) { bookmarkCollection ->
            bookmarkCollection?.let(::updateUi)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { _, insets ->
            (binding.toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            WindowInsetsCompat.CONSUMED
        }

        binding.appbar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val eightyPercent = appBar.totalScrollRange * 0.8
            binding.imageThumbnailSmall.alpha =
                verticalOffset.absoluteValue / eightyPercent.toFloat()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        binding.recyclerView.adapter = shareAdapter

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
        val intent = router.passcodeIntent(this, passcode)
        intent.putExtra(
            AppExtras.EXTRA_PASSCODE_DESCRIPTION,
            getString(R.string.dialog_bookmark_collection_picker_verify_passcode, name)
        )
        passcodeConfirmResultLauncher.launch(intent)
    }

    private fun onBookmark(shareId: String) {
        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_confirm)
            .setMessage(R.string.dialog_confirm_remove_share_from_bookmark)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.removeBookmark(shareId)
            }.setNegativeButton(R.string.dialog_cancel, null).show()
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                2 -> onBookmark(shareId)
                3 -> copy(share.boxDetail?.boxId)
            }
        }
    }

    private fun showMore(share: ShareDetail) {
        shareHelper.showMore(this, share)
    }

    private fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                openShareTextResultLauncher.launch(router.shareText(this, share.shareId))
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, shareData.uris
            )
        }
    }
}