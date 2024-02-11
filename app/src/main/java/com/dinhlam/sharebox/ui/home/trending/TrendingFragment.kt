package com.dinhlam.sharebox.ui.home.trending

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentTrendingBinding
import com.dinhlam.sharebox.extensions.buildTrendingShareModelViews
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.modelview.TextListModel
import com.dinhlam.sharebox.recyclerview.LoadMoreGridLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.LiveEventUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TrendingFragment :
    BaseViewModelFragment<TrendingState, TrendingViewModel, FragmentTrendingBinding>() {

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var router: Router

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingListModel("loading", height = ViewGroup.LayoutParams.MATCH_PARENT))
                return@getState
            }

            if (state.shares.isEmpty()) {
                add(
                    TextListModel(
                        "empty_message", getString(R.string.no_result)
                    )
                )
                return@getState
            }

            state.shares.map { shareDetail ->
                shareDetail.shareData.buildTrendingShareModelViews(
                    screenHeight(),
                    shareDetail.shareId,
                    shareDetail.shareDate,
                    shareDetail.shareNote,
                    shareDetail.user,
                    shareDetail.likeNumber,
                    commentNumber = shareDetail.commentNumber,
                    boxDetail = shareDetail.boxDetail,
                    actionOpen = ::onOpen
                )
            }.forEach { modelView ->
                modelView.attachTo(this)
            }

            if (state.isLoadingMore) {
                add(
                    LoadingListModel(
                        "load_more_${state.currentPage}",
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    }

    private fun onOpen(shareId: String) {
        startActivity(router.shareDetail(requireContext(), shareId))
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentTrendingBinding {
        return FragmentTrendingBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreGridLayoutManager(requireContext(), 2, blockShouldLoadMore = {
            return@LoadMoreGridLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }
    }

    override val viewModel: TrendingViewModel by viewModels()

    override fun onStateChanged(state: TrendingState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LiveEventUtils.eventRefreshVideosMixer.observe(viewLifecycleOwner) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.doOnPullRefresh()
            }
        }

        binding.recyclerView.layoutManager = layoutManager.apply {
            spanSizeLookup = BaseSpanSizeLookup(shareAdapter, 2)
        }
        binding.recyclerView.adapter = shareAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnPullRefresh()
        }

        viewModel.consume(this, TrendingState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }

        viewModel.consume(viewLifecycleOwner, TrendingState::showLoading) { showLoading ->
            if (showLoading) {
                binding.viewLoading.show()
            } else {
                binding.viewLoading.hide()
            }
        }
    }
}


