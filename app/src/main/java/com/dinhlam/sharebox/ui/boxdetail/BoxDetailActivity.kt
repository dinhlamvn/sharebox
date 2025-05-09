package com.dinhlam.sharebox.ui.boxdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBoxDetailBinding
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.doOnQueryTextChangedDebounce
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.openShare
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxDetailActivity :
    BaseViewModelActivity<BoxDetailState, BoxDetailViewModel, ActivityBoxDetailBinding>() {

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
        binding.iconEdit.isVisible =
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
            if (state.boxDetail == null || state.requirePasscode) {
                return@getState LoadingListModel("loading", height = 100.dp).attachTo(this)
            }

            if (state.isRefreshing) {
                LoadingListModel("top_loading", height = 50.dp).attachTo(this)
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

        binding.iconEdit.setOnClickListener {
            val boxDetail =
                getState(viewModel, BoxDetailState::boxDetail) ?: return@setOnClickListener
            editBoxResultLauncher.launch(router.boxForm(this, boxDetail.boxId))
        }

        binding.recyclerView.layoutManager = layoutManager
        shareAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            if (getState(viewModel, BoxDetailState::requirePasscode)) {
                return@setOnRefreshListener
            }
            viewModel.doOnRefresh()
        }

        binding.searchView.doOnQueryTextChangedDebounce(
            scope = lifecycleScope,
            block = viewModel::setSearchQuery
        )

        onChange(BoxDetailState::boxDetail, BoxDetailState::searchQuery) { boxDetail, _ ->
            if (boxDetail == null) {
                return@onChange
            }
            val isRequirePasscode = getState(viewModel, BoxDetailState::requirePasscode)
            if (boxDetail.isHasPasscode && isRequirePasscode) {
                val intent = router.passcodeIntent(
                    this, boxDetail.passcode,
                    desc = getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode,
                        boxDetail.boxName
                    ),
                )
                passcodeConfirmResultLauncher.launch(intent)
            } else {
                viewModel.loadShares()
            }
        }

        onChange(BoxDetailState::asyncLoadSave) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                viewModel.updateShare(asyncLoad.value)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMore(share: ShareDetail) {
        shareHelper.showMore(this, share, viewModel::loadShares)
    }

    private fun openShare(shareDetail: ShareDetail) {
        openShare(supportFragmentManager, shareDetail, router, shareHelper)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}