package com.dinhlam.sharebox.ui.boxdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBoxDetailBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxDetailActivity :
    BaseViewModelActivity<BoxDetailState, BoxDetailViewModel, ActivityBoxDetailBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener {

    private val moveShareToBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.moveShareToBox(boxId)
            }
        }

    private val changeShareNoteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.saveShareNote(result.data?.getStringExtra(Intent.EXTRA_TEXT))
            }
        }

    private val editBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { id ->
                    viewModel.reloadBoxDetail(id)
                }
            }
        }

    override fun onCreateViewBinding(): ActivityBoxDetailBinding {
        return ActivityBoxDetailBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxDetailViewModel by viewModels()

    private val openShareTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.saveShareText(result.data?.getStringExtra(Intent.EXTRA_TEXT))
            }
        }

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.loadShares()
            } else {
                showToast(R.string.error_require_passcode)
                finish()
            }
        }

    override fun onStateChanged(state: BoxDetailState) {
        binding.imageEdit.isVisible =
            userHelper.getCurrentUserId() == state.boxDetail?.createdBy
        shareAdapter.requestBuildListModels()
        binding.toolbar.title = state.boxDetail?.boxName
        binding.toolbar.subtitle = state.boxDetail?.boxDesc
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(this, blockShouldLoadMore = {
            getState(viewModel) { state ->
                state.canLoadMore && state.asyncLoadLoadMoreShares is BaseViewModel.AsyncLoad.Loading
            }
        }) {
            viewModel.loadMores()
        }
    }

    private val shareAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                LoadingListModel("top_loading").attachTo(this)
                return@getState
            }

            if (state.shares.isEmpty()) {
                TextListModel(
                    "text_empty", getString(R.string.no_result)
                ).attachTo(this)
            } else {
                state.shares.forEachIndexed { idx, shareDetail ->
                    shareDetail.buildListItemListModel(::showMore, ::openShare)
                        .attachTo(this)
                    VerticalDividerListModel(
                        "share_divider_$idx",
                        margin = Spacing.Horizontal(16.dp(), 16.dp())
                    ).attachTo(this)
                }

                LoadingListModel("home_loading_more_${state.currentPage}").attachTo(this) { state.canLoadMore && state.shares.size > 3 }
            }
        }
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var userHelper: UserHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(
                    AppExtras.EXTRA_BOX_DETAIL,
                    getState(viewModel, BoxDetailState::boxDetail)
                )
            )
            finish()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.imageEdit.setImageDrawable(Icons.editIcon2(this))
        binding.imageEdit.setOnClickListener {
            val boxDetail =
                getState(viewModel, BoxDetailState::boxDetail) ?: return@setOnClickListener
            editBoxResultLauncher.launch(router.boxForm(this, boxDetail.boxId))
        }

        binding.recyclerView.layoutManager = layoutManager
        shareAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.onChange(
            this,
            BoxDetailState::boxDetail,
            BoxDetailState::mustInputPasscode
        ) { boxDetail, mustInputPasscode ->
            if (!boxDetail?.passcode.isNullOrBlank() && mustInputPasscode) {
                val takeBox = boxDetail ?: return@onChange finish()
                val intent = router.passcodeIntent(
                    this, takeBox.passcode!!, getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode,
                        takeBox.boxName
                    )
                )
                passcodeConfirmResultLauncher.launch(intent)
            } else {
                viewModel.loadShares()
            }
        }

        viewModel.onChange(this, BoxDetailState::asyncLoadSave) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                viewModel.updateShare(asyncLoad.value)
            }
        }
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> onRequestChangeNote(share)
                2 -> onRequestMoveShare(share)
                3 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                4 -> onBookmark(shareId)
                5 -> copy(share.boxDetail?.boxId)
                6 -> moveToTrash(share)
            }
        }
    }

    private fun moveToTrash(share: ShareDetail) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_move_to_trash)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.moveShareToTrash(share.shareId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onRequestMoveShare(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        moveShareToBoxLauncher.launch(router.boxList(this, getString(R.string.move_share_to)))
    }

    private fun onRequestChangeNote(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        changeShareNoteResultLauncher.launch(
            router.textInput(
                this,
                getString(R.string.note),
                share.shareNote
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMore(share: ShareDetail) {
        shareHelper.showMore(this, share, this)
    }

    private fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                viewModel.setCurrentShare(share)
                openShareTextResultLauncher.launch(router.textInput(this, null, shareData.text))
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