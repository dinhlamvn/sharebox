package com.dinhlam.sharebox.ui.bookmark.list

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBookmarkListItemBinding
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
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

    private val shareAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.isSharesLoading) {
                LoadingListModel("loading_share").attachTo(this)
                return@getState
            }

            if (state.shares.isEmpty()) {
                TextListModel("text_empty", getString(R.string.no_result)).attachTo(this)
            } else {
                state.shares.forEach { shareDetail ->
                    shareDetail.buildListItemListModel(::showMore)
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
        binding.imageTopBar.load(this, bookmarkCollection.thumbnail)
        binding.imageThumbnailSmall.load(
            this, bookmarkCollection.thumbnail
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
        binding.toolbar.title = bookmarkCollection.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback {
            val asyncLoadRemove = getState(viewModel, BookmarkListItemState::asyncLoadRemoveShare)
            setResult(if (asyncLoadRemove.success) Activity.RESULT_OK else Activity.RESULT_CANCELED)
            finish()
        }

        viewModel.onChange(
            BookmarkListItemState::bookmarkCollection, this
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
            val alpha = verticalOffset.absoluteValue / eightyPercent.toFloat()
            binding.imageThumbnailSmall.alpha = alpha
            binding.imageTopBar.alpha = 1f.minus(alpha)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        binding.recyclerView.adapter = shareAdapter

        viewModel.onChange(
            BookmarkListItemState::requestVerifyPasscode, this
        ) { shouldRequest ->
            if (shouldRequest) {
                requestVerifyPasscode()
            }
        }

        viewModel.onChange(BookmarkListItemState::asyncLoadRemoveShare, this) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                showToast(getString(R.string.removed_item, asyncLoad.value.shareNote))
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

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return

        when (position) {
            0 -> viewModel.removeBookmark(shareId)
        }
    }

    private fun showMore(share: ShareDetail) {
        val arrayIcons = arrayOf(
            "faw_trash"
        )
        val choiceItems =
            resources.getStringArray(R.array.bookmark_collection_list_option_menu_items)
                .mapIndexed { index, text ->
                    OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                        arrayIcons[index], text
                    )
                }.toTypedArray()

        OptionMenuBottomSheetDialogFragment.show(
            supportFragmentManager,
            choiceItems,
            bundleOf(AppExtras.EXTRA_SHARE_ID to share.shareId),
            this
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}