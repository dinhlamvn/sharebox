package com.dinhlam.sharebox.ui.trash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTrashBinding
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.BottomSheetOptionsMenuDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.dp
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrashActivity :
    BaseViewModelActivity<TrashState, TrashViewModel, ActivityTrashBinding>(),
    BottomSheetOptionsMenuDialogFragment.OnOptionItemSelectedListener {

    private val restoreShareToBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.moveShareToBox(boxId)
            }
        }

    override fun onCreateViewBinding(): ActivityTrashBinding {
        return ActivityTrashBinding.inflate(layoutInflater)
    }

    override val viewModel: TrashViewModel by viewModels()

    override fun onStateChanged(state: TrashState) {
        shareAdapter.requestBuildListModels()
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

                LoadingListModel("loading_more_${state.currentPage}").attachTo(this) { state.canLoadMore && state.shares.size > 3 }
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.layoutManager = layoutManager
        shareAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> onRequestRestore(share)
            }
        }
    }

    private fun onRequestRestore(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        restoreShareToBoxLauncher.launch(router.boxList(this, getString(R.string.restore_share_to)))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMore(share: ShareDetail) {
        val arrayIcons = arrayOf(
            "f2ea"
        )
        val choiceItems =
            resources.getStringArray(R.array.trash_more_menu)
                .mapIndexed { index, text ->
                    BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                        arrayIcons[index], text
                    )
                }.toTypedArray()

        BottomSheetOptionsMenuDialogFragment.show(
            supportFragmentManager,
            choiceItems,
            bundleOf(AppExtras.EXTRA_SHARE_ID to share.shareId),
            this
        )
    }

    private fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                TextViewerDialogFragment().apply {
                    arguments = bundleOf(Intent.EXTRA_TEXT to shareData.text)
                }.show(supportFragmentManager, "TextViewerDialogFragment")
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, shareData.uris
            )

            is ShareData.ShareFile -> {
                val downloadUrl = shareData.uri.toString()
                DownloadFileDialogFragment.showDialog(
                    supportFragmentManager,
                    downloadUrl,
                    shareData.fileName,
                    shareData.mimeType
                )
            }
        }
    }
}