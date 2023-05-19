package com.dinhlam.sharebox.ui.home.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.ui.home.profile.ProfileState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager false
        }) {
            viewModel.loadMores()
        }
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("top_loading"))
                return@getState
            }
            if (state.shares.isEmpty()) {
                add(
                    TextModelView(
                        "text_empty",
                        getString(R.string.no_result)
                    )
                )
            } else {
                val models = state.shares.map { shareDetail ->
                    shareDetail.shareData.buildShareModelViews(
                        screenWidth(),
                        shareDetail.shareId,
                        shareDetail.createdAt,
                        shareDetail.shareNote,
                        shareDetail.user,
                        state.voteMap[shareDetail.shareId].orElse(0),
                        shareComment = shareDetail.commentCount,
                        bookmarked = state.bookmarkedShareIdSet.contains(shareDetail.shareId),
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionVote = ::onVote,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark
                    )
                }
                models.forEachIndexed { idx, model ->
                    add(model)
                    add(
                        SizedBoxModelView(
                            "divider_$idx",
                            height = 1.dp(),
                            backgroundColor = R.color.colorDividerLightV2
                        )
                    )
                }

                if (state.isLoadMore) {
                    add(LoadingModelView("home_load_more"))
                    add(
                        SizedBoxModelView(
                            "sizeBoxLoadMore",
                            height = 50.dp(),
                            backgroundColor = android.R.color.transparent
                        )
                    )
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, ProfileState::isLoadMore, true) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    requireContext(), shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewer(requireActivity(), shareData.text)
            }

            is ShareData.ShareImage -> {
                shareHelper.viewShareImage(requireActivity(), shareData.uri)
            }

            is ShareData.ShareImages -> {
                shareHelper.viewShareImages(requireActivity(), shareData.uris)
            }
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onVote(shareId: String) {
        viewModel.vote(shareId)
    }

    private fun onBookmark(shareId: String) {
        shareHelper.bookmark(requireActivity(), shareId) { pickedId ->
            Logger.debug("pick collection ${pickedId.toString()}")
            viewModel.bookmark(shareId, pickedId)
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showComment(childFragmentManager, shareId)
    }
}